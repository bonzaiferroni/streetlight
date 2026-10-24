package streetlight.compose.svg

/** An element of an XML document: its [name], its [attributes] and its child elements. Text content is dropped. */
internal class XmlElement(
    val name: String,
    val attributes: Map<String, String>,
    val children: List<XmlElement>,
)

/**
 * The root element of [text], read by a small reader that knows tags, attributes, comments and declarations.
 *
 * It suits regular documents such as the site's icons, and is not a general XML parser.
 */
internal fun readXml(text: String): XmlElement {
    val open = ArrayDeque<Triple<String, Map<String, String>, MutableList<XmlElement>>>()
    var root: XmlElement? = null
    fun close(element: XmlElement) {
        if (open.isEmpty()) root = element else open.last().third.add(element)
    }

    var index = 0
    while (true) {
        val start = text.indexOf('<', index)
        if (start < 0) break
        if (text.startsWith("<!--", start)) {
            val end = text.indexOf("-->", start)
            index = if (end < 0) text.length else end + 3
            continue
        }
        val end = text.indexOf('>', start)
        if (end < 0) break
        index = end + 1
        val tag = text.substring(start + 1, end)

        when {
            tag.startsWith("?") || tag.startsWith("!") -> Unit
            tag.startsWith("/") -> {
                val (name, attributes, children) = open.removeLast()
                close(XmlElement(name, attributes, children))
            }
            else -> {
                val body = tag.removeSuffix("/").trim()
                val name = body.takeWhile { !it.isWhitespace() }
                val attributes = attributeRegex.findAll(body.substring(name.length)).associate {
                    it.groupValues[1] to (it.groups[3]?.value ?: it.groups[4]?.value ?: "")
                }
                when (tag.endsWith("/")) {
                    true -> close(XmlElement(name, attributes, emptyList()))
                    false -> open.addLast(Triple(name, attributes, mutableListOf()))
                }
            }
        }
    }
    return root ?: error("no root element")
}

private val attributeRegex = Regex("""([\w:.-]+)\s*=\s*("([^"]*)"|'([^']*)')""")
