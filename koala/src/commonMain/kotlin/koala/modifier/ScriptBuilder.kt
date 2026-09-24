package koala.modifier

import koala.interop.JsFunction

/** Builds a script inside [jsScriptOf]. */
class ScriptBuilder(private val builder: StringBuilder) {
    /** Adds the declaration of [function]. */
    fun define(function: JsFunction) {
        builder.appendLine(function.definition)
        builder.appendLine()
    }

    /** Adds a call of [function] with [args]. */
    fun invoke(function: JsFunction, vararg args: Any) {
        builder.appendLine(function.invokeJs(*args))
    }
}

/** A script built by [block]. */
fun jsScriptOf(block: ScriptBuilder.() -> Unit) = buildString {
    val builder = ScriptBuilder(this)
    builder.block()
}