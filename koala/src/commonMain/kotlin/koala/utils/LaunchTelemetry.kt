package koala.utils

import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

/** The name of a launched coroutine, carried in its context for error messages. */
class LaunchTelemetry(
    val name: String,
) : AbstractCoroutineContextElement(Key) {
    companion object Key : CoroutineContext.Key<LaunchTelemetry>
}