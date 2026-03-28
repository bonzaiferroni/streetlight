package koala.css

data class CssUtility(
    override val identifier: String,
    val definition: String? = null,
    ): Modifier

fun List<CssUtility>.toStylesheet() = mapNotNull{ it.definition }.joinToString("\n")

fun utilityOf(identifier: String, vararg properties: String): CssUtility {
    val definition = buildString {
        append('.')
        append(identifier)
        append(" { ")
        properties.forEach {
            append(it)
            append("; ")
        }
        append("}")
    }
    return CssUtility(identifier, definition)
}