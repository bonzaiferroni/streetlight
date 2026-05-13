package koala.dom

import koala.html.Id
import kotlinx.browser.document
import kotlinx.coroutines.CoroutineScope
import kotlinx.dom.clear
import kotlinx.html.dom.append
import kotlinx.html.dom.prepend
import org.w3c.dom.HTMLElement

interface RenderContext: DOMContext {
    val app: AppContext
    val renderScope: CoroutineScope
}

class DOMRenderContext(
    consumer: DOMContext,
    override val app: AppContext,
    override val renderScope: CoroutineScope,
    val parent: HTMLElement,
): RenderContext, DOMContext by consumer

fun HTMLElement.renderRoot(
    scope: CoroutineScope,
    app: AppContext,
    block: RenderContext.() -> Unit
): List<HTMLElement> {
    clear()
    return append {
        val context = DOMRenderContext(this@append, app, getScope(scope, true), this@renderRoot)
        context.block()
    }
}

fun RenderContext.replaceRender(
    element: HTMLElement,
    block: RenderContext.() -> Unit
): List<HTMLElement> {
    element.clear()
    return element.append {
        val context = DOMRenderContext(this@append, app, element.getScope(renderScope, true), element)
        context.block()
    }
}

fun RenderContext.clearRender(element: HTMLElement) {
    element.clear()
    element.clearScope()
}

fun RenderContext.appendRender(
    element: HTMLElement,
    block: RenderContext.() -> Unit
) = element.append {
    val context = DOMRenderContext(this@append, app, element.getScope(renderScope, false), element)
    context.block()
}

fun RenderContext.prependRender(
    element: HTMLElement,
    block: RenderContext.() -> Unit
) = element.prepend {
    val context = DOMRenderContext(this@prepend, app, element.getScope(renderScope, false), element)
    context.block()
}

fun RenderContext.replaceRender(
    id: Id,
    ancestor: HTMLElement? = null,
    block: RenderContext.() -> Unit
) = replaceRender(((ancestor ?: document.body!!).querySelector(id) ?: error("element not found: $this")), block)