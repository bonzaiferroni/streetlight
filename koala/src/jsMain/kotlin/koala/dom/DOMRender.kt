package koala.dom

import koala.html.Id
import kotlinx.browser.document
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.dom.clear
import kotlinx.html.dom.append
import kotlinx.html.dom.prepend
import org.w3c.dom.HTMLElement

interface DOMRender: DOM {
    val app: AppContext
    val renderScope: CoroutineScope
    val parent: HTMLElement

    fun launchRender(block: suspend CoroutineScope.() -> Unit) {
        renderScope.launch {
            try {
                block()
            } catch(e: UnsupportedOperationException) {
                throw InvalidRenderOperation(parent)
            }
        }
    }
}

class DOMRenderContext(
    consumer: DOM,
    override val app: AppContext,
    override val renderScope: CoroutineScope,
    override val parent: HTMLElement,
): DOMRender, DOM by consumer

fun HTMLElement.renderRoot(
    scope: CoroutineScope,
    app: AppContext,
    block: DOMRender.() -> Unit
): List<HTMLElement> {
    clear()
    return append {
        val context = DOMRenderContext(this@append, app, provisionScope(scope, true), this@renderRoot)
        context.block()
    }
}

fun DOMRender.replaceRender(
    element: HTMLElement,
    block: DOMRender.() -> Unit,
): List<HTMLElement> {
    element.clear()
    return element.append {
        val context = DOMRenderContext(this@append, app, element.provisionScope(renderScope, true), element)
        context.block()
    }
}

fun DOMRender.clearRender(element: HTMLElement) {
    element.clear()
    element.clearScope()
}

fun DOMRender.appendRender(
    element: HTMLElement,
    block: DOMRender.() -> Unit
) = element.append {
    val context = DOMRenderContext(this@append, app, element.provisionScope(renderScope, false), element)
    context.block()
}

fun DOMRender.prependRender(
    element: HTMLElement,
    block: DOMRender.() -> Unit
) = element.prepend {
    val context = DOMRenderContext(this@prepend, app, element.provisionScope(renderScope, false), element)
    context.block()
}

fun DOMRender.replaceRender(
    id: Id,
    ancestor: HTMLElement? = null,
    block: DOMRender.() -> Unit
) = replaceRender(((ancestor ?: document.body!!).querySelector(id) ?: error("element not found: $this")), block)

class InvalidRenderOperation(parent: HTMLElement): Exception("Appended to finalized element: ${parent.domPath()}")