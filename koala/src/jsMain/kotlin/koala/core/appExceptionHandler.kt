package koala.core

import koala.dom.getPath
import kotlinx.coroutines.CoroutineExceptionHandler
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

val appExceptionHandler = CoroutineExceptionHandler { context, throwable ->
    try {
        throwable.asDynamic().message = buildString {
            appendLine(throwable.message ?: "[No message]")
            val scope = context[ScopeTelemetry]
            scope?.let { telemetry ->
                val path = telemetry.getPath()
                append("Scope: ")
                appendLine(path)
            }
            val launch = context[LaunchTelemetry]
            launch?.let { telemetry ->
                append("Launch: ")
                appendLine(telemetry.name)
            }
            (launch?.element ?: scope?.element)?.let { element ->
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

class ScopeTelemetry(
    val name: String,
    val element: Element? = null,
    val parent: ScopeTelemetry? = null,
) : AbstractCoroutineContextElement(Key) {
    companion object Key : CoroutineContext.Key<ScopeTelemetry>
}

fun ScopeTelemetry.getPath(limit: Int = Int.MAX_VALUE): String = buildString {
    var count = 0
    var current: ScopeTelemetry? = this@getPath
    while (current != null) {
        if (++count > limit) break
        if (isNotEmpty()) insert(0, " > ")
        insert(0, current.name)
        current = current.parent
    }
}