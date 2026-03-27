package koala.css

data class CssUtility(
    override val identifier: String,
    val definition: String? = null,
    ): Modifier

fun List<CssUtility>.toStylesheet() = mapNotNull{ it.definition }.joinToString("\n")
