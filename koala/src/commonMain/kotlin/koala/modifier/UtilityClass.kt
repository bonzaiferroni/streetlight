package koala.modifier

data class UtilityClass(
    val cssClass: Class,
    val definition: String,
    ): ClassModifier by cssClass {
    override fun toString() = cssClass.selector

    companion object {
        operator fun invoke(name: String, definition: String) = UtilityClass(Class(name), definition)
    }
}

fun List<UtilityClass>.toStylesheet() = mapNotNull{ it.definition }.joinToString("\n")

fun utilityOf(identifier: String, vararg properties: String): UtilityClass {
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
    return UtilityClass(Class(identifier), definition)
}