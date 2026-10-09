package koala

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

/** The [SvgPack] of [label], each variant named `label-variant.svg`. */
fun svgPackOf(label: String) = SvgPack(
    label = label,
    small = svgOf("$label-small"),
    large = svgOf("$label-large"),
    veryLarge = svgOf("$label-very-large"),
    filled = svgOf("$label-filled"),
    animated = svgOf("$label-animated"),
)
