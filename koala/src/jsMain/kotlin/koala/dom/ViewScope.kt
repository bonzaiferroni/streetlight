package koala.dom

import kampfire.model.Messenger
import kotlinx.coroutines.CoroutineScope
import kotlinx.dom.clear
import kotlinx.html.dom.append
import org.w3c.dom.HTMLElement

@ViewMarker
sealed interface ViewScope: TagScope, AppFacade {
    override val app: AppContainer
    val name: String
    val mount: HTMLElement
    val scope: CoroutineScope
    val contentScope: CoroutineScope
    val parent: ViewScope?

    fun onDispose(block: () -> Unit)

    fun launchEffect(
        name: String = "ViewScope.launchEffect",
        receiver: Messenger? = null,
        message: String? = "Something went wrong.",
        block: suspend EffectScope.() -> Unit
    ) = contentScope.launch(name, receiver, message) {
        EffectScope(this@ViewScope).block()
    }

    fun launchViewEffect(
        name: String = "ViewScope.launchViewEffect",
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

interface RebuildScope: DelegatedViewScope {

    fun rebuildContent(block: ViewScope.() -> Unit) {
        val view = resolveView()
        view.clear()
        mount.clear()
        mount.append {
            view.setConsumer(this)
            block()
        }
    }
}