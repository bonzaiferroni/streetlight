package koala.dom

import koala.html.Id
import kotlinx.browser.document
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.dom.clear
import kotlinx.html.dom.append
import kotlinx.html.dom.prepend
import org.w3c.dom.HTMLElement

interface RenderContext: DOMContext {
    val renderScope: CoroutineScope
}

class DOMRenderContext(
    consumer: DOMContext,
    override val renderScope: CoroutineScope,
    val parent: HTMLElement,
): RenderContext, DOMContext by consumer

fun HTMLElement.replaceRender(
    scope: CoroutineScope,
    block: RenderContext.() -> Unit
): List<HTMLElement> {
    clear()
    return append {
        val context = DOMRenderContext(this, getElementScope(scope, true), this@replaceRender)
        context.block()
    }
}

fun HTMLElement.clearRender() {
    clear()
    clearScope()
}

fun HTMLElement.appendRender(
    scope: CoroutineScope,
    block: RenderContext.() -> Unit
) = append {
    val context = DOMRenderContext(this, getElementScope(scope, false), this@appendRender)
    context.block()
}

fun HTMLElement.prependRender(
    scope: CoroutineScope,
    block: RenderContext.() -> Unit
) = prepend {
    val context = DOMRenderContext(this, getElementScope(scope, false), this@prependRender)
    context.block()
}

fun Id.replaceRender(
    scope: CoroutineScope,
    block: RenderContext.() -> Unit
) = (document.body!!.querySelector(this) ?: error("element not found: $this")).replaceRender(scope, block)