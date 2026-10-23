package koala.markdown

import koala.lottiePath

private val safeLinkSchemes = setOf("http", "https", "mailto")

/**
 * True when [url] is safe to link from user markdown: `http`, `https`, `mailto`, or no scheme, as a site path or an
 * anchor. Whitespace and control characters are ignored, as a browser ignores them in a scheme.
 */
fun isSafeMarkdownUrl(url: String): Boolean {
    val compact = url.filterNot { it.isWhitespace() || it < ' ' }
    val colon = compact.indexOf(':')
    if (colon < 0) return true
    val beforeColon = compact.substring(0, colon)
    if (beforeColon.any { it == '/' || it == '?' || it == '#' }) return true
    return beforeColon.lowercase() in safeLinkSchemes
}

/** True when [path] names one of the site's own Lottie files, the only animations a page loads. */
fun isSiteLottiePath(path: String): Boolean =
    path.startsWith(lottiePath) && path.endsWith(".json") && ".." !in path && '\\' !in path && ':' !in path
