package koala.dom

import web.html.HTMLElement

private val NORMALIZE = Regex("[\u00A0\u200B\uFEFF\r]|\r\n")

fun HTMLElement.normalizedTextContent(): String {
    val text = textContent ?: return ""
    if (!NORMALIZE.containsMatchIn(text)) return text
    return NORMALIZE.replace(text) {
        when (it.value) {
            "\u00A0" -> " "
            "\r\n", "\r" -> "\n"
            else -> ""
        }
    }
}

