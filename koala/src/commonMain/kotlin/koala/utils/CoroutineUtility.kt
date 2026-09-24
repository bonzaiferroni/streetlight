package koala.utils

import kampfire.model.Messenger
import kampfire.model.UIMessage
import kampfire.model.UIMessageType
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.reflect.KClass
import kotlin.reflect.KFunction

/**
 * Launches [block] named [name], so an exception in it names where it came from. A failure delivers [message] to
 * [messenger] and rethrows.
 */
fun CoroutineScope.launch(
    name: String,
    messenger: Messenger? = null,
    message: String? = "Something went wrong.",
    block: suspend CoroutineScope.() -> Unit,
): Job {
    // engraved at the call site, while the true stack still stands
    // val launchSite = Throwable("Launched: $name")

    return launch(LaunchTelemetry(name)) {
        try {
            block()
        } catch (e: CancellationException) {
            throw e // sacred cargo
        } catch (e: Throwable) {
            if (message != null) messenger?.deliver(UIMessage(message, UIMessageType.Error))
            // e.asDynamic().launchStack = launchSite.stack
            throw e
        }
    }
}

/** [launch], named for [function]. */
fun CoroutineScope.launch(
    function: KFunction<*>,
    receiver: Messenger? = null,
    message: String? = "Something went wrong.",
    block: suspend CoroutineScope.() -> Unit,
) = launch(function.name, receiver, message, block)

/** [launch], named for [context]. */
fun CoroutineScope.launch(
    context: KClass<*>,
    receiver: Messenger? = null,
    message: String? = "Something went wrong.",
    block: suspend CoroutineScope.() -> Unit,
) = launch(context.simpleName ?: "[Unnamed class]", receiver, message, block)