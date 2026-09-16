package koala.interop

data class JsFunction(
    override val name: String,
    val definition: String,
): GlobalFunction

fun jsFunctionOf(definition: String) = JsFunction(
    name = parseName(definition),
    definition = definition
)

private val functionNameRegex = Regex("""^\s*(?:async\s+)?function\s+([A-Za-z_$][\w$]*)\s*\(""")

private fun parseName(definition: String): String =
    functionNameRegex.find(definition)?.groupValues?.get(1)
        ?: error("Not a named function declaration: ${definition.take(40)}")

data class JsSignature(override val name: String): GlobalFunction