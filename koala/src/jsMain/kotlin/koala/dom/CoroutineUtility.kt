package koala.dom

import kampfire.model.MessageReceiver
import kampfire.model.UIMessage
import kampfire.model.UIMessageType
import koala.core.LaunchTelemetry
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

fun CoroutineScope.launch(
    name: String,
    receiver: MessageReceiver? = null,
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
            if (message != null) receiver?.receive(UIMessage(message, UIMessageType.Error))
            // e.asDynamic().launchStack = launchSite.stack
            throw e
        }
    }
}