package com.watchface.idtool.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.watchface.idtool.AppSettings
import com.watchface.idtool.BgMode
import com.watchface.idtool.ui.AppLocale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.math.PI

// ====================================================================
// 「墨白」极简设计系统 v4（Ink & Paper · Modern Minimal）
//
// 设计语言：现代 SaaS 工具风（Linear / Notion 式的克制）——
//   · 纯白平面卡片 + 1px 发丝描边，不再使用玻璃折射与光晕
//   · 单一靛蓝主色，语义色仅用于状态表达
//   · 按钮体系：墨色实心主按钮 / 白底描边次按钮 / 红描边危险按钮
//   · 删除雪花、全局点击光效、微光扫过等全部装饰性动效
//
// 层次结构：
//   L0  AppBackground      极浅中性灰 #F6F6F8
//   L1  GlassCard          白色平面卡片（发丝描边）
//   L2  GlassButton/Chip   墨色 / 描边按钮与芯片
//   L3  GlassNavBar        白色圆角浮动导航条
// ====================================================================

/** 全局语义色 */
object AppColors {
    val success = IOSPalette.success
    val warning = IOSPalette.warning
    val danger = IOSPalette.destructive
    val info = IOSPalette.tint

    @Composable
    fun successAdaptive(): Color = success

    @Composable
    fun dangerAdaptive(): Color = danger

    @Composable
    fun infoAdaptive(): Color = info
}

/** 兼容保留：极简模式下组件不再消费折射采样源（MainActivity 仍会提供） */
val LocalAppBackdrop = staticCompositionLocalOf<LayerBackdrop?> { null }

/**
 * 兼容保留：原液态玻璃跟手倾斜传感器。
 * 极简模式下为空实现，仅维持 MainActivity 的生命周期接线不报错。
 */
object LiquidGlassTilt {
    fun start(@Suppress("UNUSED_PARAMETER") context: Context) {}
    fun stop() {}
}

// ====================================================================
// 材质修饰符（签名保留 · 视觉扁平化）
// ====================================================================

/**
 * 原液态玻璃折射。极简模式下为 no-op：所有参数保留以兼容调用点，
 * 材质由 [glass] 的平面白卡 + 发丝描边承担。
 */
fun Modifier.liquidGlass(
    @Suppress("UNUSED_PARAMETER") shape: Shape,
    @Suppress("UNUSED_PARAMETER") blurRadius: Dp = 12.dp,
    @Suppress("UNUSED_PARAMETER") lensHeight: Dp = 18.dp,
    @Suppress("UNUSED_PARAMETER") lensAmount: Dp = 30.dp,
    @Suppress("UNUSED_PARAMETER") depthEffect: Boolean = true,
    @Suppress("UNUSED_PARAMETER") chromaticAberration: Boolean = true,
    @Suppress("UNUSED_PARAMETER") layerBlock: (androidx.compose.ui.graphics.GraphicsLayerScope.() -> Unit)? = null
): Modifier = this

/** 原按压增强折射玻璃面板。极简模式下为 no-op。 */
fun Modifier.liquidGlassPanel(
    @Suppress("UNUSED_PARAMETER") backdrop: Backdrop,
    @Suppress("UNUSED_PARAMETER") shape: Shape,
    @Suppress("UNUSED_PARAMETER") pressProgress: () -> Float,
    @Suppress("UNUSED_PARAMETER") lensHeight: Dp = 10.dp,
    @Suppress("UNUSED_PARAMETER") lensAmount: Dp = 14.dp
): Modifier = this

/** 平面材质参数（签名保留） */
data class GlassColors(
    val tintTop: Color,
    val tintBottom: Color,
    val highlight: Color,
    val rimBright: Color,
    val rimDim: Color
)

/** 极简平面材质：默认纯白填充 + 发丝描边 */
@Composable
fun rememberGlassColors(
    tintTop: Color? = null,
    tintBottom: Color? = null
): GlassColors = GlassColors(
    tintTop = tintTop ?: IOSPalette.card,
    tintBottom = tintBottom ?: IOSPalette.card,
    highlight = Color.Transparent,
    rimBright = IOSPalette.separator,
    rimDim = IOSPalette.separator
)

/**
 * 极简平面材质绘制：
 *   1. 纯色/上下微渐变填充（默认纯白）
 *   2. 1px 发丝描边（颜色取 rimDim，alpha 跟随 borderAlpha）
 */
@Composable
fun Modifier.glass(
    shape: Shape,
    colors: GlassColors,
    borderAlpha: Float = 1f,
    @Suppress("UNUSED_PARAMETER") highlightAlpha: Float = 1f
): Modifier {
    return this.drawBehind {
        val outline = shape.createOutline(size, layoutDirection, this)

        // 1. 平面填充（支持上下微渐变，默认纯白）
        drawOutline(
            outline = outline,
            brush = Brush.verticalGradient(
                colors = listOf(colors.tintTop, colors.tintBottom),
                startY = 0f,
                endY = size.height
            )
        )

        // 2. 1px 发丝描边
        if (borderAlpha > 0f) {
            drawOutline(
                outline = outline,
                color = colors.rimDim.copy(alpha = colors.rimDim.alpha * borderAlpha),
                style = Stroke(width = 1.dp.toPx())
            )
        }
    }
}

/** 极简弥散投影：极轻一层，仅用于弹窗等浮层（签名保留） */
@Composable
fun Modifier.glassShadow(
    elevation: Dp,
    shape: Shape
): Modifier = this.shadow(
    elevation = elevation,
    shape = shape,
    clip = false,
    spotColor = Color.Black.copy(alpha = 0.06f),
    ambientColor = Color.Black.copy(alpha = 0.03f)
)

/** 原微光扫过。极简模式下为 no-op（签名保留）。 */
@Composable
fun Modifier.shimmerSweep(
    @Suppress("UNUSED_PARAMETER") periodMillis: Int = 2800,
    @Suppress("UNUSED_PARAMETER") bandColor: Color = Color.White
): Modifier = this

/** 原外发光光晕。极简模式下为 no-op（签名保留）。 */
fun Modifier.glow(
    @Suppress("UNUSED_PARAMETER") color: Color,
    @Suppress("UNUSED_PARAMETER") radiusFraction: Float = 1.1f
): Modifier = this

/** 按压弹簧缩放（保留：极简风格下的轻按压手感） */
@Composable
fun Modifier.pressScale(
    interactionSource: MutableInteractionSource,
    pressedScale: Float = 0.97f
): Modifier {
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) pressedScale else 1f,
        animationSpec = spring(
            dampingRatio = 0.62f,
            stiffness = 1400f
        ),
        label = "pressScale"
    )
    return this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

/** 原能量波纹。极简模式下为 no-op（签名保留）。 */
@Composable
fun Modifier.pressRipple(
    @Suppress("UNUSED_PARAMETER") interactionSource: MutableInteractionSource,
    @Suppress("UNUSED_PARAMETER") clipShape: Shape? = null,
    @Suppress("UNUSED_PARAMETER") color: Color = Color.White,
    @Suppress("UNUSED_PARAMETER") intensity: Float = 1f
): Modifier = this

/** 呼吸脉冲（用于状态点等小元素） */
@Composable
fun Modifier.pulse(minScale: Float = 0.85f, maxScale: Float = 1.18f, period: Int = 1600): Modifier {
    val transition = androidx.compose.animation.core.rememberInfiniteTransition(label = "pulse")
    val t by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            tween(period, easing = androidx.compose.animation.core.LinearEasing)
        ),
        label = "pulseT"
    )
    val scale = minScale + (maxScale - minScale) * (0.5f + 0.5f * kotlin.math.sin(t * 2f * PI.toFloat()))
    return this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}


/**
 * 原液态动态背景。极简模式下统一为静态浅灰底（签名保留）。
 */
@Composable
fun LiquidBackground(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize().background(IOSPalette.groupedBackground))
}

/** 应用背景：支持自定义壁纸（图片模式下压白蒙版保证可读性），默认极浅灰 */
@Composable
fun AppBackground(modifier: Modifier = Modifier) {
    val cfg = AppSettings.bgConfig
    when (cfg.mode) {
        BgMode.COLOR -> ColorBackground(cfg.color, modifier)

        BgMode.GALLERY -> {
            val context = LocalContext.current
            val path = remember { AppSettings.customWallpaperFile(context).absolutePath }
            val bitmap by produceState<Bitmap?>(initialValue = null, path) {
                value = withContext(Dispatchers.IO) {
                    runCatching {
                        val bound = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                        BitmapFactory.decodeFile(path, bound)
                        var sample = 1
                        while (kotlin.math.max(bound.outWidth, bound.outHeight) / (sample * 2) >= 1440) sample *= 2
                        BitmapFactory.decodeFile(
                            path,
                            BitmapFactory.Options().apply { inSampleSize = sample }
                        )
                    }.getOrNull()
                }
            }
            val bmp = bitmap
            if (bmp != null) {
                Box(modifier.fillMaxSize()) {
                    Image(
                        bitmap = bmp.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    PhotoScrim()
                }
            } else {
                FlatBackground(modifier)
            }
        }

        else -> FlatBackground(modifier)
    }
}

/** 图片提亮蒙版：压一层白，保证白卡片与深色文字可读 */
@Composable
private fun PhotoScrim(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(IOSPalette.groupedBackground.copy(alpha = 0.68f))
    )
}

/** 纯色背景 */
@Composable
private fun ColorBackground(color: Long, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(color))
    )
}

/** 极简默认背景：纯净 #F6F6F8，无颗粒噪点、无暗角 */
@Composable
private fun FlatBackground(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(IOSPalette.groupedBackground)
    )
}

/**
 * 原降雪动效层。极简模式下为空实现（签名保留，调用点不渲染任何内容）。
 */
@Composable
fun SnowfallLayer(
    @Suppress("UNUSED_PARAMETER") modifier: Modifier = Modifier,
    @Suppress("UNUSED_PARAMETER") farCount: Int = 34,
    @Suppress("UNUSED_PARAMETER") midCount: Int = 30,
    @Suppress("UNUSED_PARAMETER") nearCount: Int = 20
) = Unit

/**
 * 原全局点击光效。极简模式下为空实现（签名保留）。
 */
@Composable
fun GlobalRippleOverlay(@Suppress("UNUSED_PARAMETER") modifier: Modifier = Modifier) = Unit

// ====================================================================
// L1 卡片
// ====================================================================

/**
 * 极简白卡：纯白填充 + 1px 发丝描边，无阴影。
 * 可点击时按压态为轻微变淡（与全局按压语言一致）。
 */
@Composable
fun GlassCard(
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(IOSPalette.cardRadius),
    @Suppress("UNUSED_PARAMETER") tintTop: Color? = null,
    @Suppress("UNUSED_PARAMETER") tintBottom: Color? = null,
    contentPadding: Dp = 16.dp,
    shadowElevation: Dp = 4.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val clickInteraction = remember(onClick != null) { MutableInteractionSource() }
    val cardPressed by clickInteraction.collectIsPressedAsState()
    val cardAlpha by animateFloatAsState(
        targetValue = if (cardPressed && onClick != null) IOSPalette.pressedOpacity else 1f,
        animationSpec = tween(90),
        label = "cardPressAlpha"
    )
    val base = if (onClick != null) {
        Modifier.clickable(interactionSource = clickInteraction, indication = null) {
            onClick()
        }
    } else {
        Modifier
    }
    Column(
        modifier = modifier
            .graphicsLayer { alpha = cardAlpha }
            .then(base)
            .glassShadow(shadowElevation, shape)
            .glass(
                shape,
                rememberGlassColors()
            )
            .padding(contentPadding),
        content = content
    )
}

// ====================================================================
// L2 按钮
// ====================================================================

enum class GlassButtonStyle { Primary, Glass, Danger }

/**
 * 极简按钮体系（签名保留）：
 *   Primary = 墨色实心（#17181C）+ 白字 —— 现代简约标志性的黑色主按钮
 *   Glass   = 纯白底 + 1px 发丝描边 + 近黑文字 —— 次级按钮
 *   Danger  = 纯白底 + 1px 红描边 + 红字 —— 危险操作
 */
@Composable
fun GlassButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    style: GlassButtonStyle = GlassButtonStyle.Primary,
    height: Dp = 48.dp,
    @Suppress("UNUSED_PARAMETER") shimmer: Boolean = true
) {
    val interaction = remember { MutableInteractionSource() }
    val shape = RoundedCornerShape(13.dp)
    val pressed by interaction.collectIsPressedAsState()
    val pressAlpha by animateFloatAsState(
        targetValue = if (pressed) IOSPalette.pressedOpacity else 1f,
        animationSpec = tween(90),
        label = "buttonPressAlpha"
    )

    val container = when (style) {
        GlassButtonStyle.Primary -> Modifier
            .clip(shape)
            .background(IOSPalette.label)

        GlassButtonStyle.Glass -> Modifier
            .clip(shape)
            .background(IOSPalette.card)
            .drawBehind {
                val outline = shape.createOutline(size, layoutDirection, this)
                drawOutline(outline, color = IOSPalette.separator, style = Stroke(width = 1.dp.toPx()))
            }

        GlassButtonStyle.Danger -> Modifier
            .clip(shape)
            .background(IOSPalette.card)
            .drawBehind {
                val outline = shape.createOutline(size, layoutDirection, this)
                drawOutline(outline, color = IOSPalette.destructive, style = Stroke(width = 1.dp.toPx()))
            }
    }

    val contentColor = when (style) {
        GlassButtonStyle.Primary -> Color.White
        GlassButtonStyle.Glass -> IOSPalette.label
        GlassButtonStyle.Danger -> IOSPalette.destructive
    }

    Box(
        modifier = modifier
            .height(height)
            .graphicsLayer { alpha = pressAlpha }
            .then(container)
            .clickable(interactionSource = interaction, indication = null) {
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(17.dp)
                )
                Spacer(Modifier.width(7.dp))
            }
            Text(
                text = text,
                color = contentColor,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                letterSpacing = 0.1.sp,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        }
    }
}

/** 圆形图标按钮：白底 + 发丝描边（极简） */
@Composable
fun GlassIconButton(
    icon: ImageVector,
    contentDescription: String?,
    tint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 34.dp,
    @Suppress("UNUSED_PARAMETER") tintTop: Color? = null,
    @Suppress("UNUSED_PARAMETER") tintBottom: Color? = null
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val iconBtnAlpha by animateFloatAsState(
        targetValue = if (pressed) IOSPalette.pressedOpacity else 1f,
        animationSpec = tween(90),
        label = "iconBtnAlpha"
    )
    Box(
        modifier = modifier
            .graphicsLayer { alpha = iconBtnAlpha }
            .glass(CircleShape, rememberGlassColors())
            .clickable(interactionSource = interaction, indication = null) {
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(size * 0.47f)
        )
    }
}

/**
 * 极简芯片：选中 = 靛蓝实心白字；未选中 = 白底描边灰字。
 */
@Composable
fun GlassChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interaction = remember { MutableInteractionSource() }
    val shape = RoundedCornerShape(50)

    val containerColor by androidx.compose.animation.animateColorAsState(
        targetValue = if (selected) IOSPalette.tint else IOSPalette.card,
        animationSpec = tween(180),
        label = "chipFill"
    )
    val contentColor by androidx.compose.animation.animateColorAsState(
        targetValue = if (selected) Color.White else IOSPalette.secondaryLabel,
        animationSpec = tween(180),
        label = "chipText"
    )
    val scale by animateFloatAsState(
        targetValue = if (selected) 1f else 0.97f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 500f),
        label = "chipScale"
    )
    val chipPressed by interaction.collectIsPressedAsState()
    val chipPressScale by animateFloatAsState(
        targetValue = if (chipPressed) 0.94f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 1400f),
        label = "chipPressScale"
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale * chipPressScale
                scaleY = scale * chipPressScale
            }
            .clip(shape)
            .background(containerColor)
            .drawBehind {
                if (!selected) {
                    val outline = shape.createOutline(size, layoutDirection, this)
                    drawOutline(outline, color = IOSPalette.separator, style = Stroke(width = 1.dp.toPx()))
                }
            }
            .clickable(interactionSource = interaction, indication = null) { onClick() }
            .padding(horizontal = 15.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = contentColor,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            maxLines = 1
        )
    }
}

/** 扁平状态点（去光晕，保留轻微呼吸） */
@Composable
fun GlowDot(color: Color, modifier: Modifier = Modifier, dotSize: Dp = 8.dp) {
    Box(
        modifier
            .size(dotSize)
            .pulse(minScale = 0.92f, maxScale = 1.08f, period = 2000)
            .background(color, CircleShape)
    )
}

// ====================================================================
// L3 底部导航栏（极简 · 白色圆角浮动条）
//
//   · 白色圆角浮动条 + 1px 发丝描边（去阴影、去胶囊测量动画）
//   · Tab：图标在上、文字在下；选中 = 靛蓝，未选中 = 次级灰
//   · 右侧独立圆形「设置」按钮：白底描边，选中 = 靛蓝填充白图标
// ====================================================================

data class GlassNavTab(val icon: ImageVector, val label: String)

@Composable
fun GlassNavBar(
    tabs: List<GlassNavTab>,
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    onSettingsClick: (() -> Unit)? = null,
    /** 当前是否停留在设置页：为 true 时右侧齿轮呈选中态 */
    settingsSelected: Boolean = false
) {
    val safeIndex = if (selected in tabs.indices) selected else -1
    val navShape = RoundedCornerShape(IOSPalette.tabBarRadius)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .glass(navShape, rememberGlassColors())
            .padding(horizontal = 10.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        tabs.forEachIndexed { index, tab ->
            val isSelected = index == safeIndex
            val tabTint by androidx.compose.animation.animateColorAsState(
                targetValue = if (isSelected) IOSPalette.tint else IOSPalette.secondaryLabel,
                animationSpec = tween(180),
                label = "navTabTint"
            )
            val tabInteraction = remember { MutableInteractionSource() }
            val tabPressed by tabInteraction.collectIsPressedAsState()

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(IOSPalette.rowRadius))
                    .clickable(
                        interactionSource = tabInteraction,
                        indication = null
                    ) { onSelect(index) }
                    .graphicsLayer { alpha = if (tabPressed) IOSPalette.pressedOpacity else 1f }
                    .padding(vertical = 5.dp)
            ) {
                Icon(
                    imageVector = tab.icon,
                    contentDescription = tab.label,
                    tint = tabTint,
                    modifier = Modifier.size(21.dp)
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    text = tab.label,
                    fontSize = 10.sp,
                    lineHeight = 12.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                    color = tabTint,
                    maxLines = 1
                )
            }
        }

        if (onSettingsClick != null) {
            Spacer(Modifier.width(6.dp))
            val settingsInteraction = remember { MutableInteractionSource() }
            val settingsPressed by settingsInteraction.collectIsPressedAsState()
            val gearTint by androidx.compose.animation.animateColorAsState(
                targetValue = if (settingsSelected) Color.White else IOSPalette.secondaryLabel,
                animationSpec = tween(180),
                label = "gearTint"
            )
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .graphicsLayer { alpha = if (settingsPressed) IOSPalette.pressedOpacity else 1f }
                    .then(
                        if (settingsSelected) {
                            Modifier
                                .clip(CircleShape)
                                .background(IOSPalette.tint)
                        } else {
                            Modifier
                                .clip(CircleShape)
                                .background(IOSPalette.card)
                                .drawBehind {
                                    val outline = CircleShape.createOutline(size, layoutDirection, this)
                                    drawOutline(outline, color = IOSPalette.separator, style = Stroke(width = 1.dp.toPx()))
                                }
                        }
                    )
                    .clickable(
                        interactionSource = settingsInteraction,
                        indication = null
                    ) { onSettingsClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "设置",
                    tint = gearTint,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/** 圆形导航按钮（极简：白底描边 / 选中 = 靛蓝填充白图标） */
@Composable
fun GlassFabButton(
    icon: ImageVector,
    contentDescription: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 46.dp,
    iconSize: Dp = 20.dp
) {
    val interaction = remember { MutableInteractionSource() }
    val iconColor by androidx.compose.animation.animateColorAsState(
        targetValue = if (selected) Color.White else IOSPalette.secondaryLabel,
        animationSpec = tween(180),
        label = "fabIconColor"
    )
    val iconRotation by animateFloatAsState(
        targetValue = if (selected) 90f else 0f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 260f),
        label = "fabIconRotation"
    )
    val pressed by interaction.collectIsPressedAsState()

    Box(
        modifier = modifier
            .size(size)
            .graphicsLayer { alpha = if (pressed) IOSPalette.pressedOpacity else 1f }
            .then(
                if (selected) {
                    Modifier
                        .clip(CircleShape)
                        .background(IOSPalette.tint)
                } else {
                    Modifier
                        .clip(CircleShape)
                        .background(IOSPalette.card)
                        .drawBehind {
                            // this.size = DrawScope 的 Size（外层 size 参数为 Dp，已被遮蔽）
                            val outline = CircleShape.createOutline(this.size, layoutDirection, this)
                            drawOutline(outline, color = IOSPalette.separator, style = Stroke(width = 1.dp.toPx()))
                        }
                }
            )
            .clickable(interactionSource = interaction, indication = null) {
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            icon,
            contentDescription = contentDescription,
            tint = iconColor,
            modifier = Modifier
                .size(iconSize)
                .graphicsLayer { rotationZ = iconRotation }
        )
    }
}

/** 列表入场（保留轻量上滑淡入） */
@Composable
fun StaggeredItem(
    index: Int,
    modifier: Modifier = Modifier,
    delayPerItem: Int = 55,
    content: @Composable () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(index.coerceAtMost(14).toLong() * delayPerItem)
        visible = true
    }
    androidx.compose.animation.AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(260)) +
            androidx.compose.animation.slideInVertically(tween(260)) { it / 5 },
        modifier = modifier
    ) {
        androidx.compose.foundation.layout.Box { content() }
    }
}

/** 弹窗入场动画（缩放 + 淡入） */
@Composable
private fun DialogEntrance(content: @Composable () -> Unit) {
    var shown by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { shown = true }
    androidx.compose.animation.AnimatedVisibility(
        visible = shown,
        enter = scaleIn(
            initialScale = 0.88f,
            animationSpec = spring(dampingRatio = 0.72f, stiffness = 420f)
        ) + fadeIn(tween(160)),
        exit = fadeOut(tween(140))
    ) {
        androidx.compose.foundation.layout.Box { content() }
    }
}

// ====================================================================
// 加载遮罩 / 加载点
// ====================================================================

@Composable
fun LoadingOverlay(text: String) {
    var shown by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { shown = true }

    androidx.compose.animation.AnimatedVisibility(
        visible = shown,
        enter = fadeIn(tween(180)),
        exit = fadeOut(tween(150))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.28f)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .glassShadow(10.dp, RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                    .background(IOSPalette.card)
                    .padding(horizontal = 26.dp, vertical = 22.dp)
            ) {
                LiquidDotsLoader()
                Spacer(Modifier.height(14.dp))
                Text(
                    text = text,
                    fontSize = 13.5.sp,
                    color = IOSPalette.secondaryLabel
                )
            }
        }
    }
}

/** 三点波浪加载器 */
@Composable
fun LiquidDotsLoader(dotSize: Dp = 12.dp, color: Color = IOSPalette.tint) {
    val transition = androidx.compose.animation.core.rememberInfiniteTransition(label = "loader")
    val t by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            tween(1300, easing = androidx.compose.animation.core.LinearEasing)
        ),
        label = "loaderT"
    )
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(3) { i ->
            val phase = t * 2f * PI.toFloat() + i * (2f * PI.toFloat() / 3f)
            val wave = kotlin.math.max(0f, kotlin.math.sin(phase))
            val scale = 0.55f + 0.45f * wave
            Box(
                Modifier
                    .size(dotSize)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        alpha = 0.35f + 0.65f * wave
                    }
                    .background(color, CircleShape)
            )
        }
    }
}

// ====================================================================
// 结果弹窗 / 确认弹窗
// ====================================================================

@Composable
fun ResultDialog(
    success: Boolean,
    message: String,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        DialogEntrance {
            GlassCard(
                shape = RoundedCornerShape(20.dp),
                contentPadding = 22.dp,
                shadowElevation = 12.dp
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    var iconShown by remember { mutableStateOf(false) }
                    LaunchedEffect(Unit) {
                        delay(80)
                        iconShown = true
                    }
                    androidx.compose.animation.AnimatedVisibility(
                        visible = iconShown,
                        enter = scaleIn(
                            initialScale = 0.2f,
                            animationSpec = spring(
                                dampingRatio = 0.38f,
                                stiffness = 520f
                            )
                        ) + fadeIn(tween(160))
                    ) {
                        // 极简图标章：语义色 12% 圆底 + 语义色图标
                        val accent = if (success) AppColors.successAdaptive() else AppColors.dangerAdaptive()
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape)
                                .background(accent.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (success) Icons.Default.Check else Icons.Default.Close,
                                contentDescription = if (success) "成功" else "失败",
                                tint = accent,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = if (success) "操作成功" else "操作失败",
                        fontSize = 16.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = IOSPalette.label
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = message,
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                        color = IOSPalette.secondaryLabel,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(20.dp))
                    GlassButton(
                        text = "好",
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth(),
                        style = GlassButtonStyle.Primary
                    )
                }
            }
        }
    }
}

@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmText: String = "删除",
    /** 可选复选项文本（如「同时删除本地文件」），null 则不显示 */
    checkboxText: String? = null,
    checked: Boolean = false,
    onCheckedChange: ((Boolean) -> Unit)? = null,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        DialogEntrance {
            GlassCard(
                shape = RoundedCornerShape(20.dp),
                contentPadding = 22.dp,
                shadowElevation = 12.dp
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // 警示图标章：警告色 12% 圆底
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(IOSPalette.warning.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "警告",
                            tint = IOSPalette.warning,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = title,
                        fontSize = 16.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = IOSPalette.label
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = message,
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                        color = IOSPalette.secondaryLabel,
                        textAlign = TextAlign.Center
                    )
                    if (checkboxText != null && onCheckedChange != null) {
                        Spacer(Modifier.height(16.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(IOSPalette.rowRadius))
                                .clickable { onCheckedChange(!checked) }
                                .padding(horizontal = 4.dp, vertical = 6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(RoundedCornerShape(5.dp))
                                    .then(
                                        if (checked) Modifier.background(IOSPalette.tint)
                                        else Modifier.background(IOSPalette.card)
                                    )
                                    .drawBehind {
                                        if (!checked) {
                                            val outline = RoundedCornerShape(5.dp)
                                                .createOutline(size, layoutDirection, this)
                                            drawOutline(
                                                outline,
                                                color = IOSPalette.separator,
                                                style = Stroke(width = 1.2.dp.toPx())
                                            )
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (checked) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = checkboxText,
                                fontSize = 13.sp,
                                color = IOSPalette.secondaryLabel
                            )
                        }
                    }
                    Spacer(Modifier.height(20.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        GlassButton(
                            text = "取消",
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            style = GlassButtonStyle.Glass,
                            height = 44.dp
                        )
                        GlassButton(
                            text = confirmText,
                            onClick = onConfirm,
                            modifier = Modifier.weight(1f),
                            style = GlassButtonStyle.Danger,
                            height = 44.dp
                        )
                    }
                }
            }
        }
    }
}

// ====================================================================
// Toast
// ====================================================================

@Composable
fun ToastMessage(message: String, onFinished: () -> Unit) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(message) {
        visible = true
        delay(2200)
        visible = false
        delay(300)
        onFinished()
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        androidx.compose.animation.AnimatedVisibility(
            visible = visible,
            modifier = Modifier.padding(bottom = 108.dp),
            enter = fadeIn(tween(200)) + androidx.compose.animation.slideInVertically(
                animationSpec = spring(dampingRatio = 0.7f, stiffness = 500f)
            ) { it / 2 },
            exit = fadeOut(tween(220)) + androidx.compose.animation.slideOutVertically(tween(240)) { it / 3 }
        ) {
            // 墨色胶囊：与主按钮同色的深墨底 + 白字
            Box(
                modifier = Modifier
                    .shadow(6.dp, RoundedCornerShape(50))
                    .clip(RoundedCornerShape(50))
                    .background(IOSPalette.label.copy(alpha = 0.94f))
            ) {
                Text(
                    text = message,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.2.sp,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 11.dp)
                )
            }
        }
    }
}

// ====================================================================
// 工具
// ====================================================================

internal fun formatBytes(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB")
    var size = bytes.toDouble()
    var unitIndex = 0
    while (size >= 1024 && unitIndex < units.size - 1) {
        size /= 1024
        unitIndex++
    }
    return String.format("%.1f %s", size, units[unitIndex])
}

/** 秒数 → 可读时长（跟随界面语言） */
internal fun formatDuration(seconds: Long): String {
    if (seconds < 0) return AppLocale.t("0 秒")
    if (seconds < 60) return AppLocale.tf("{0} 秒", seconds)
    val minutes = seconds / 60
    val remain = seconds % 60
    return if (remain == 0L) AppLocale.tf("{0} 分钟", minutes)
    else AppLocale.tf("{0} 分 {1} 秒", minutes, remain)
}
