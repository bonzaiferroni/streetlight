package koala.dom

import kampfire.model.MessageReceiver
import koala.core.LaunchTelemetry
import koala.html.Id
import kotlinx.browser.document
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.dom.clear
import kotlinx.html.dom.append
import kotlinx.html.dom.prepend
import org.w3c.dom.HTMLElement

@RenderMarker
interface ViewScope: TagScope, AppFacade {
    override val app: AppContainer
    val parent: HTMLElement
    val parentScope: CoroutineScope

    fun launchEffect(
        name: String = "ViewScope.launchEffect",
        receiver: MessageReceiver? = null,
        message: String? = "Something went wrong.",
        block: suspend EffectScope.() -> Unit
    ) = parentScope.launch(name, receiver, message) {
        EffectScope(this@ViewScope).block()
    }
}

interface AppFacade {
    val app: AppContainer
}

@DslMarker
annotation class RenderMarker

@RenderMarker
class EffectScope(
    val view: ViewScope,
): AppFacade {
    override val app get() = view.app
    val parentScope get() = view.parentScope

    fun launch(
        name: String = "EffectScope.launch",
        receiver: MessageReceiver? = null,
        message: String? = "Something went wrong.",
        block: suspend CoroutineScope.() -> Unit
    ) = parentScope.launch(name, receiver, message) { block() }
}

class View(
    consumer: TagScope,
    override val app: AppContainer,
    override val parentScope: CoroutineScope,
    override val parent: HTMLElement,
): ViewScope, TagScope by consumer

fun HTMLElement.renderRoot(
    name: String,
    scope: CoroutineScope,
    app: AppContainer,
    block: ViewScope.() -> Unit
): List<HTMLElement> {
    clear()
    return append {
        val context = View(this@append, app, provisionScope(name, scope, true), this@renderRoot)
        context.block()
    }
}

fun HTMLElement.replaceDynamicRender(
    name: String,
    app: AppContainer,
    parentScope: CoroutineScope,
    block: ViewScope.() -> Unit,
): List<HTMLElement> {
    clear()
    return append {
        val context = View(this@append, app, provisionScope(name, parentScope, true), this@replaceDynamicRender)
        context.block()
    }
}

fun ViewScope.replaceDynamicRender(
    name: String,
    element: HTMLElement,
    block: ViewScope.() -> Unit,
) = element.replaceDynamicRender(name, app, parentScope, block)


fun HTMLElement.replaceStaticRender(
    app: AppContainer,
    parentScope: CoroutineScope,
    block: ViewScope.() -> Unit,
): List<HTMLElement> {
    clear()
    return append {
        val scope = View(this, app, parentScope, this@replaceStaticRender)
        scope.block()
    }
}

fun clearRender(element: HTMLElement) {
    element.clear()
    element.clearScope()
}

fun HTMLElement.appendRender(
    name: String,
    app: AppContainer,
    parentScope: CoroutineScope,
    block: ViewScope.() -> Unit
) = append {
    val context = View(this@append, app, provisionScope(name, parentScope, false), this@appendRender)
    context.block()
}

fun ViewScope.appendRender(
    name: String,
    element: HTMLElement,
    block: ViewScope.() -> Unit
) = element.appendRender(name, app, parentScope, block)

fun ViewScope.prependRender(
    name: String,
    element: HTMLElement,
    block: ViewScope.() -> Unit
) = element.prepend {
    val context = View(this@prepend, app, element.provisionScope(name, parentScope, false), element)
    context.block()
}

fun ViewScope.replaceDynamicRender(
    id: Id,
    ancestor: HTMLElement? = null,
    block: ViewScope.() -> Unit
) = replaceDynamicRender(id.identifier, ((ancestor ?: document.body!!).querySelector(id) ?: error("element not found: $this")), block)

class InvalidRenderOperation(parent: HTMLElement): Exception("Appended to finalized element: ${parent.getPath()}")