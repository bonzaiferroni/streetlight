package koala.dom

import kampfire.model.Messenger
import koala.utils.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.dom.clear
import kotlinx.html.dom.append
import org.w3c.dom.HTMLElement
import kotlin.reflect.KClass
import kotlin.reflect.KFunction

@ViewMarker
sealed interface ViewScope: AppendScope, AppFacade {
    override val app: AppContainer
    val scopeName: String
    val mount: HTMLElement
    val scope: CoroutineScope
    val contentScope: CoroutineScope
    val parent: ViewScope?

    fun onDispose(block: () -> Unit)

    fun launchEffect(
        name: String = ::launchEffect.name,
        messenger: Messenger? = null,
        message: String? = GENERAL_ERROR_MESSAGE,
        block: suspend EffectScope.() -> Unit
    ) = contentScope.launch(name, messenger, message) {
        EffectScope(this@ViewScope).block()
    }

    fun launchViewEffect(
        name: String = ::launchViewEffect.name,
        messenger: Messenger? = null,
        message: String? = GENERAL_ERROR_MESSAGE,
        block: suspend EffectScope.() -> Unit
    ) = scope.launch(name, messenger, message) {
        EffectScope(this@ViewScope).block()
    }
}

fun ViewScope.launchEffect(
    function: KFunction<*>,
    messenger: Messenger? = null,
    message: String? = GENERAL_ERROR_MESSAGE,
    block: suspend EffectScope.() -> Unit
) = launchEffect(function.name, messenger, message, block)

fun ViewScope.launchEffect(
    type: KClass<*>,
    messenger: Messenger? = null,
    message: String? = GENERAL_ERROR_MESSAGE,
    block: suspend EffectScope.() -> Unit
) = launchEffect(type.simpleName ?: "launchEffect", messenger, message, block)

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

const val GENERAL_ERROR_MESSAGE = "Something went wrong."