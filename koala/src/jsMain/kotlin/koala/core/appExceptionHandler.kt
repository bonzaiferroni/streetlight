package koala.core

import koala.dom.View
import koala.dom.ViewScope
import koala.dom.getPath
import koala.utils.LaunchTelemetry
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

/**
 * Adds the name of the failing coroutine and the path of its view and element to an exception's message, then
 * rethrows it.
 */
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

/** The view a coroutine belongs to, carried in its context for error messages. */
class ViewTelemetry(
    val view: View,
) : AbstractCoroutineContextElement(Key) {
    companion object Key : CoroutineContext.Key<ViewTelemetry>
}

/** The names of this view and its ancestors, from the root. */
fun ViewScope.getPath(): String =
    generateSequence(this) { it.parent }
        .toList().asReversed()
        .joinToString(" > ") { it.scopeName }