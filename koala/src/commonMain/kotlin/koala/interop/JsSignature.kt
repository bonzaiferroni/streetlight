package koala.interop

/** A JS function written in JS, with the [definition] a script declares it by. */
data class JsFunction(
    override val name: String,
    val definition: String,
): GlobalFunction

/** A [JsFunction] of [definition], named by the function it declares. */
fun jsFunctionOf(definition: String) = JsFunction(
    name = parseName(definition),
    definition = definition
)

private val functionNameRegex = Regex("""^\s*(?:async\s+)?function\s+([A-Za-z_$][\w$]*)\s*\(""")

private fun parseName(definition: String): String =
    functionNameRegex.find(definition)?.groupValues?.get(1)
        ?: error("Not a named function declaration: ${definition.take(40)}")

/** The name of a global function defined by Kotlin, as a [KtFunction] in the browser. */
data class JsSignature(override val name: String): GlobalFunction