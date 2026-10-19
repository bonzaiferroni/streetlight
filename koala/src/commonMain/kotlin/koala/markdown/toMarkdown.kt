package koala.markdown

import koala.Svg

/** The markdown image syntax that renders this as an icon, with [altText] as its caption and [width] in units. */
fun Svg.toMarkdown(altText: String = "", width: Int? = null): String {
    val widthArg = width?.let { " width=$it" } ?: ""
    return "![$altText](${url.toRelativePath()} icon$widthArg)"
}
