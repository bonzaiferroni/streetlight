package koala.dom

import kampfire.model.Messenger
import koala.utils.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.dom.clear
import kotlinx.html.dom.append
import web.html.HTMLElement
import kotlin.dom.clear
import kotlin.reflect.KClass
import kotlin.reflect.KFunction

/**
 * The receiver of every view function: an append target, the app's services, and the scopes that end with the
 * view.
 *
 * [scope] lives until the view is disposed. [contentScope] lives until the view's content is cleared or
 * replaced.
 */
@ViewMarker
sealed interface ViewScope: AppendScope, AppFacade {
    override val app: AppContainer
    val scopeName: String
    val mount: HTMLElement
    val scope: CoroutineScope
    val contentScope: CoroutineScope
    val parent: ViewScope?

    /** Runs [block] when the view is disposed or its content is cleared. */
    fun onDispose(block: () -> Unit)

    /**
     * Launches [block] in [contentScope], so it ends when the content is replaced. A failure delivers [message]
     * to [messenger] when one is given.
     */
    fun launchEffect(
        name: String = ::launchEffect.name,
        messenger: Messenger? = null,
        message: String? = GENERAL_ERROR_MESSAGE,
        block: suspend EffectScope.() -> Unit
    ) = contentScope.launch(name, messenger, message) {
        EffectScope(this@ViewScope).block()
    }

    /** Launches [block] in [scope], so it survives a content rebuild and ends with the view. */
    fun launchViewEffect(
        name: String = ::launchViewEffect.name,
        messenger: Messenger? = null,
        message: String? = GENERAL_ERROR_MESSAGE,
        block: suspend EffectScope.() -> Unit
    ) = scope.launch(name, messenger, message) {
        EffectScope(this@ViewScope).block()
    }
}

/** [launchEffect], named for [function]. */
fun ViewScope.launchEffect(
    function: KFunction<*>,
    messenger: Messenger? = null,
    message: String? = GENERAL_ERROR_MESSAGE,
    block: suspend EffectScope.() -> Unit
) = launchEffect(function.name, messenger, message, block)

/** [launchEffect], named for [type]. */
fun ViewScope.launchEffect(
    type: KClass<*>,
    messenger: Messenger? = null,
    message: String? = GENERAL_ERROR_MESSAGE,
    block: suspend EffectScope.() -> Unit
) = launchEffect(type.simpleName ?: "launchEffect", messenger, message, block)

/** A [ViewScope] that forwards to another view. */
interface DelegatedViewScope: ViewScope {
    val viewDelegate: ViewScope
}

/** The [View] behind this scope, through any delegates. */
tailrec fun ViewScope.resolveView(): View = when (this) {
    is View -> this
    is DelegatedViewScope -> viewDelegate.resolveView()
}

/** Keeps an outer view scope from being used implicitly inside a nested one. */
@DslMarker
annotation class ViewMarker

/** A delegated scope that can replace the content of its view. */
interface RebuildScope: DelegatedViewScope {

    /** Clears the view's content and builds it again with [block]. */
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