package koala.dom

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
interface RenderScope: AppendScope {
    val app: AppContainer
    val parent: HTMLElement
    val parentScope: CoroutineScope

    fun launchEffect(block: suspend EffectScope.() -> Unit) {
        val effectScope = EffectScope(app, parent, parentScope)
        parentScope.launch { effectScope.block() }
    }
}

@DslMarker
annotation class RenderMarker

@RenderMarker
class EffectScope(
    val app: AppContainer,
    val parent: HTMLElement,
    val parentScope: CoroutineScope,
) {
    fun launch(block: suspend CoroutineScope.() -> Unit): Job =
        parentScope.launch {
            try { block() }
            catch (e: UnsupportedOperationException) {
                throw InvalidRenderOperation(parent)
            }
        }
}

class DOMRenderContext(
    consumer: AppendScope,
    override val app: AppContainer,
    override val parentScope: CoroutineScope,
    override val parent: HTMLElement,
): RenderScope, AppendScope by consumer

fun HTMLElement.renderRoot(
    scope: CoroutineScope,
    app: AppContainer,
    block: RenderScope.() -> Unit
): List<HTMLElement> {
    clear()
    return append {
        val context = DOMRenderContext(this@append, app, provisionScope(scope, true), this@renderRoot)
        context.block()
    }
}

fun HTMLElement.replaceRender(
    app: AppContainer,
    parentScope: CoroutineScope,
    block: RenderScope.() -> Unit,
): List<HTMLElement> {
    clear()
    return append {
        val context = DOMRenderContext(this@append, app, provisionScope(parentScope, true), this@replaceRender)
        context.block()
    }
}

fun RenderScope.replaceRender(
    element: HTMLElement,
    block: RenderScope.() -> Unit,
) = element.replaceRender(app, parentScope, block)

fun RenderScope.clearRender(element: HTMLElement) {
    element.clear()
    element.clearScope()
}

fun HTMLElement.appendRender(
    app: AppContainer,
    parentScope: CoroutineScope,
    block: RenderScope.() -> Unit
) = append {
    val context = DOMRenderContext(this@append, app, provisionScope(parentScope, false), this@appendRender)
    context.block()
}

fun RenderScope.appendRender(
    element: HTMLElement,
    block: RenderScope.() -> Unit
) = element.appendRender(app, parentScope, block)

fun RenderScope.prependRender(
    element: HTMLElement,
    block: RenderScope.() -> Unit
) = element.prepend {
    val context = DOMRenderContext(this@prepend, app, element.provisionScope(parentScope, false), element)
    context.block()
}

fun RenderScope.replaceRender(
    id: Id,
    ancestor: HTMLElement? = null,
    block: RenderScope.() -> Unit
) = replaceRender(((ancestor ?: document.body!!).querySelector(id) ?: error("element not found: $this")), block)

class InvalidRenderOperation(parent: HTMLElement): Exception("Appended to finalized element: ${parent.domPath()}")