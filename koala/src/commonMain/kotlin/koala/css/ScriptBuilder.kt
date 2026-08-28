package koala.css

import koala.interop.JsFunction

class ScriptBuilder(private val builder: StringBuilder) {
    fun define(function: JsFunction) {
        builder.appendLine(function.definition)
        builder.appendLine()
    }

    fun invoke(function: JsFunction, vararg args: Any) {
        builder.appendLine(function.invokeJs(*args))
    }
}

fun jsScriptOf(block: ScriptBuilder.() -> Unit) = buildString {
    val builder = ScriptBuilder(this)
    builder.block()
}