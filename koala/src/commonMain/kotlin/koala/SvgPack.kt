package koala

import kampfire.model.toUrl

/** The variants of an icon, declared once by [label]. */
data class SvgPack(
    val label: String,
    val small: Svg,
    val large: Svg,
    val veryLarge: Svg,
    val filled: Svg,
    val animated: Svg,
): Asset {
    override val url get() = small.url
    override val assetType get() = AssetType.Svg
}

/** The [SvgPack] of [label], each variant at `label-variant.svg` in [iconPath]. */
fun svgPackOf(label: String) = SvgPack(
    label = label,
    small = Svg("$iconPath$label-small.svg".toUrl()),
    large = Svg("$iconPath$label-large.svg".toUrl()),
    veryLarge = Svg("$iconPath$label-very-large.svg".toUrl()),
    filled = Svg("$iconPath$label-filled.svg".toUrl()),
    animated = Svg("$iconPath$label-animated.svg".toUrl()),
)
