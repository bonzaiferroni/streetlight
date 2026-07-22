package koala.utils

import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

class LaunchTelemetry(
    val name: String,
) : AbstractCoroutineContextElement(Key) {
    companion object Key : CoroutineContext.Key<LaunchTelemetry>
}