package com.watchface.idtool.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.watchface.idtool.R

// ====================================================================
// 字体（保留 Barlow 几何无衬线，全语言覆盖）
// ====================================================================

val AppFonts = FontFamily(
    Font(R.font.barlow_regular, weight = FontWeight.Normal),
    Font(R.font.barlow_medium, weight = FontWeight.Medium),
    Font(R.font.barlow_medium, weight = FontWeight.SemiBold),
    Font(R.font.barlow_regular, weight = FontWeight.Bold)
)

/** 数字/ID 专用：窄长机械数字（表盘 ID、版本号、计数器） */
val NumericFonts = FontFamily(
    Font(R.font.barlow_regular, weight = FontWeight.Normal),
    Font(R.font.barlow_medium, weight = FontWeight.Medium)
)

// ====================================================================
// 「墨白」极简设计令牌（Ink & Paper Design Tokens）
//
// 设计语言：现代 SaaS 工具风 —— 大面积留白、纯白平面卡片、
// 1px 发丝描边代替投影、单一靛蓝主色、克制的语义色。
//
// 层级模型（从下到上）：
//   groupedBackground  #F6F6F8  页面底色（极浅中性灰）
//   card               #FFFFFF  纯白平面卡片（1px #E7E7EB 描边）
//   separator          #E7E7EB  1px 分割线 / 描边
//   label              #17181C  主文本（近黑，比纯黑柔和）
//   secondaryLabel     #6E717A  次级文本 / 数值
//   tint               #3D6DF5  唯一主色（现代靛蓝）
//   capsuleSelected    #EDF1FE  选中态浅靛蓝底
// ====================================================================

object IOSPalette {

    // ---- 层级底色 ----
    /** 页面底色（极浅中性灰，衬托白卡） */
    val groupedBackground = Color(0xFFF6F6F8)

    /** 卡片底色（纯白平面） */
    val card = Color(0xFFFFFFFF)

    /** 次级卡片 / 嵌插槽位底（如输入框、代码块） */
    val cardSecondary = Color(0xFFFAFAFB)

    /** 底部导航条底色 */
    val tabBar = Color(0xFFFFFFFF)

    /** 选中态底（浅靛蓝，替代半透明灰光效） */
    val capsuleSelected = Color(0xFFEDF1FE)

    // ---- 分割线 ----
    /** 1px 分割线 / 卡片描边 */
    val separator = Color(0xFFE7E7EB)

    /** 更柔和的分割线变体（卡片内缩进场景） */
    val separatorSoft = Color(0xFFF0F0F3)

    // ---- 文本 ----
    /** 主文本：近黑（比纯黑更柔和，长文本不刺眼） */
    val label = Color(0xFF17181C)
    val secondaryLabel = Color(0xFF6E717A)
    val tertiaryLabel = Color(0xFFA9ACB4)

    // ---- 高亮 ----
    /** 唯一主色：现代靛蓝 */
    val tint = Color(0xFF3D6DF5)
    val destructive = Color(0xFFE5484D)
    val success = Color(0xFF1DA75A)
    val warning = Color(0xFFE28800)

    // ---- 彩色图标瓦片底（低饱和现代色板）----
    val tileRed = Color(0xFFE5484D)
    val tileOrange = Color(0xFFE28800)
    val tileYellow = Color(0xFFD9B60F)
    val tileGreen = Color(0xFF1DA75A)
    val tileBlue = Color(0xFF3D6DF5)
    val tilePurple = Color(0xFF8B5CF6)
    val tileTeal = Color(0xFF0E9BA8)
    val tileIndigo = Color(0xFF5B5BD6)
    val tileGray = Color(0xFF6E717A)
    val purple = Color(0xFF8B5CF6)

    /** 瓦片内图标统一白色 */
    val tileGlyph = Color(0xFFFFFFFF)

    // ---- 交互 ----
    /** 按下时整体透明度（平面卡片的轻按压态） */
    val pressedOpacity = 0.72f
    val pressedOpacityStrong = 0.55f

    // ---- 圆角（现代简约：中等圆角，干净利落）----
    /** 卡片圆角 */
    val cardRadius = 18.dp
    /** ListItem 高亮块圆角 */
    val rowRadius = 10.dp
    /** 彩色图标瓦片圆角 */
    val tileRadius = 8.dp
    /** 底部导航条圆角 */
    val tabBarRadius = 22.dp
    /** 圆形设置按钮半径 */
    val fabRadius = 21.dp
}

// ====================================================================
// Material3 颜色方案（映射到「墨白」令牌）
// ====================================================================

private val MinimalLightColors = lightColorScheme(
    primary = IOSPalette.tint,
    onPrimary = Color.White,
    primaryContainer = IOSPalette.capsuleSelected,
    onPrimaryContainer = IOSPalette.tint,
    secondary = IOSPalette.secondaryLabel,
    onSecondary = Color.White,
    secondaryContainer = IOSPalette.cardSecondary,
    onSecondaryContainer = IOSPalette.label,
    tertiary = IOSPalette.purple,
    onTertiary = Color.White,
    tertiaryContainer = IOSPalette.cardSecondary,
    onTertiaryContainer = IOSPalette.label,
    error = IOSPalette.destructive,
    onError = Color.White,
    errorContainer = Color(0xFFFDECEC),
    onErrorContainer = IOSPalette.destructive,
    background = IOSPalette.groupedBackground,
    onBackground = IOSPalette.label,
    surface = IOSPalette.card,
    onSurface = IOSPalette.label,
    surfaceVariant = IOSPalette.cardSecondary,
    onSurfaceVariant = IOSPalette.secondaryLabel,
    outline = IOSPalette.separator,
    outlineVariant = IOSPalette.separator
)

// ====================================================================
// 排版（现代简约规格：紧凑行高、中强字重层级）
// ====================================================================

private fun minimalTextStyle(
    weight: FontWeight,
    size: Float,
    line: Float,
    spacing: Float = 0f
) = TextStyle(
    fontFamily = AppFonts,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = line.sp,
    letterSpacing = spacing.sp
)

private val MinimalTypography = Typography(
    displayLarge = minimalTextStyle(FontWeight.Bold, 28, 34, -0.3f),
    displayMedium = minimalTextStyle(FontWeight.Bold, 24, 30, -0.3f),
    displaySmall = minimalTextStyle(FontWeight.Bold, 22, 28, -0.2f),
    headlineLarge = minimalTextStyle(FontWeight.SemiBold, 21, 27, -0.2f),
    headlineMedium = minimalTextStyle(FontWeight.SemiBold, 19, 25, -0.2f),
    headlineSmall = minimalTextStyle(FontWeight.SemiBold, 17, 22),
    titleLarge = minimalTextStyle(FontWeight.Medium, 17, 22, -0.2f),
    titleMedium = minimalTextStyle(FontWeight.Medium, 15.5, 21, -0.1f),
    titleSmall = minimalTextStyle(FontWeight.Medium, 14.5, 20, -0.1f),
    bodyLarge = minimalTextStyle(FontWeight.Normal, 16, 22, -0.1f),
    bodyMedium = minimalTextStyle(FontWeight.Normal, 14.5, 20, -0.1f),
    bodySmall = minimalTextStyle(FontWeight.Normal, 13, 18),
    labelLarge = minimalTextStyle(FontWeight.Medium, 14.5, 20, -0.1f),
    labelMedium = minimalTextStyle(FontWeight.Normal, 13, 18, -0.05f),
    labelSmall = minimalTextStyle(FontWeight.Normal, 11, 14, 0.06f)
)

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(20.dp)
)

/**
 * 「墨白」极简浅色主题（固定浅色）。
 */
@Composable
fun WatchFaceTheme(
    @Suppress("UNUSED_PARAMETER") darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = MinimalLightColors,
        typography = MinimalTypography,
        shapes = AppShapes,
        content = content
    )
}
