package koala.dom

import kampfire.model.Messenger
import koala.utils.launch
import kotlinx.coroutines.CoroutineScope

/** The receiver of a view effect, launching coroutines in the view's content scope. */
@ViewMarker
class EffectScope(
    val view: ViewScope,
): AppFacade {
    override val app get() = view.app
    val parentScope get() = view.contentScope

    /** Launches [block] in the view's content scope. A failure delivers [message] to [receiver] when one is given. */
    fun launch(
        name: String = "EffectScope.launch",
        receiver: Messenger? = null,
        message: String? = "Something went wrong.",
        block: suspend CoroutineScope.() -> Unit
    ) = parentScope.launch(name, receiver, message) { block() }
}