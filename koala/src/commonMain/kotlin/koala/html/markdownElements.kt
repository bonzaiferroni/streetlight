package koala.html

import kotlinx.html.FlowContent

fun FlowContent.markdownElements(markdown: String) {
    val chunks = markdown.split("\n\n")
    chunks.forEach { markdownElement(it) }
}

fun FlowContent.markdownElement(chunk: String) {
    if (headingElement(chunk)) return
    textBlock(chunk.trim())
}

fun FlowContent.headingElement(chunk: String): Boolean {
    headingPrefixSet.forEach { prefix ->
        if (chunk.startsWith(prefix)) {
            val text = chunk.drop(prefix.length).trim()
            when (prefix.length) {
                5 -> heading5(text)
                4 -> heading4(text)
                3 -> filigree { heading3(text) }
                2 -> filigree { heading2(text) }
                1 -> filigree { heading1(text) }
                else -> error("invalid heading: $chunk")
            }
            return true
        }
    }
    return false
}

private val headingPrefixSet = setOf(
    "#####",
    "####",
    "###",
    "##",
    "#"
)
