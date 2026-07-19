package koala.dom

import kampfire.model.Messenger
import koala.html.Id
import kotlinx.browser.document
import kotlinx.coroutines.CoroutineScope
import kotlinx.dom.clear
import kotlinx.html.dom.append
import kotlinx.html.dom.prepend
import org.w3c.dom.HTMLElement

@ViewMarker
sealed interface ViewScope: TagScope, AppFacade {
    override val app: AppContainer
    val name: String
    val mount: HTMLElement
    val scope: CoroutineScope
    val parent: ViewScope?

    fun onDispose(block: () -> Unit)

    fun launchEffect(
        name: String = "ViewScope.launchEffect",
        receiver: Messenger? = null,
        message: String? = "Something went wrong.",
        block: suspend EffectScope.() -> Unit
    ) = scope.launch(name, receiver, message) {
        EffectScope(this@ViewScope).block()
    }
}

interface DelegatedViewScope: ViewScope {
    val viewDelegate: ViewScope
}

tailrec fun ViewScope.resolveView(): View = when (this) {
    is View -> this
    is DelegatedViewScope -> viewDelegate.resolveView()
}

@DslMarker
annotation class ViewMarker

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