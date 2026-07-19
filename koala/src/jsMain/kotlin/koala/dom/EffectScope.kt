package koala.dom

import kampfire.model.Messenger
import kotlinx.coroutines.CoroutineScope

@ViewMarker
class EffectScope(
    val view: ViewScope,
): AppFacade {
    override val app get() = view.app
    val parentScope get() = view.scope

    fun launch(
        name: String = "EffectScope.launch",
        receiver: Messenger? = null,
        message: String? = "Something went wrong.",
        block: suspend CoroutineScope.() -> Unit
    ) = parentScope.launch(name, receiver, message) { block() }

    // fun onDispose(block: (Throwable?) -> Unit) = parentScope.coroutineContext.job.invokeOnCompletion(block)
}