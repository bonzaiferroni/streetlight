package koala.core

import koala.dom.View
import koala.dom.ViewScope
import koala.dom.getPath
import kotlinx.coroutines.CoroutineExceptionHandler
import org.w3c.dom.Element
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

val appExceptionHandler = CoroutineExceptionHandler { context, throwable ->
    try {
        throwable.asDynamic().message = buildString {
            appendLine(throwable.message ?: "[No message]")
            val viewTelemetry = context[ViewTelemetry]
            viewTelemetry?.let { telemetry ->
                val path = telemetry.view.getPath()
                append("View: ")
                appendLine(path)
            }
            val launchTelemetry = context[LaunchTelemetry]
            launchTelemetry?.let { telemetry ->
                append("Launch: ")
                appendLine(telemetry.name)
            }
            (launchTelemetry?.element ?: viewTelemetry?.view?.mount)?.let { element ->
                val elementPath = element.getPath()
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

class LaunchTelemetry(
    val name: String,
    val element: Element? = null,
) : AbstractCoroutineContextElement(Key) {
    companion object Key : CoroutineContext.Key<LaunchTelemetry>
}

class ViewTelemetry(
    val view: View,
) : AbstractCoroutineContextElement(Key) {
    companion object Key : CoroutineContext.Key<ViewTelemetry>
}

fun ViewScope.getPath(): String =
    generateSequence(this) { it.parent }
        .toList().asReversed()
        .joinToString(" > ") { it.name }