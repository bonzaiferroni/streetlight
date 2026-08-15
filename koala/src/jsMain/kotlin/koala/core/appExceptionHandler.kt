package koala.core

import koala.dom.View
import koala.dom.ViewScope
import koala.dom.getPath
import koala.utils.LaunchTelemetry
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

val appExceptionHandler = CoroutineExceptionHandler { context, throwable ->
    try {
        throwable.asDynamic().message = buildString {
            appendLine(throwable.message ?: "[No message]")
                context[LaunchTelemetry]?.let { telemetry ->
                append("Launch: ")
                appendLine(telemetry.name)
            }
            context[ViewTelemetry]?.view?.let { view ->
                val path = view.getPath()
                append("View: ")
                appendLine(path)
                val elementPath = view.mount.getPath()
                append("Element: ")
                appendLine(elementPath)
            }
        }
    } catch (telemetryThrowable: Throwable) {
        console.log("Error constructing telemetry: ${telemetryThrowable.message}")
    }

    // note to future-Luke: rethrowing the SAME throwable falls through
    // unwrapped to the default last-resort handler, preserving its display
    throw throwable
}

class ViewTelemetry(
    val view: View,
) : AbstractCoroutineContextElement(Key) {
    companion object Key : CoroutineContext.Key<ViewTelemetry>
}

fun ViewScope.getPath(): String =
    generateSequence(this) { it.parent }
        .toList().asReversed()
        .joinToString(" > ") { it.scopeName }