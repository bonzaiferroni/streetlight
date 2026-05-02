package koala.dom

import koala.html.Id
import kotlinx.browser.document
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.dom.clear
import kotlinx.html.FlowContent
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

class RenderCache(
    val context: RenderContext,
    val job: Job,
    val localScope: CoroutineScope,
    val elements: List<HTMLElement>
) {
    val firstElement get() = elements.first()
}

fun <T> createRender(
    parent: HTMLElement,
    scope: CoroutineScope,
    value: T,
    block: RenderContext.(T) -> Unit
): RenderCache {
    val job = SupervisorJob()
    val localScope = CoroutineScope(scope.coroutineContext + job)
    var context: RenderContext
    val elements = parent.append {
        context = DOMRenderContext(this, localScope, parent)
        context.block(value)
    }
    return RenderCache(context, job, localScope, elements)
}

fun createRender(
    parent: HTMLElement,
    scope: CoroutineScope = CoroutineScope(Dispatchers.Main),
    block: RenderContext.() -> Unit
) = createRender(parent, scope, Unit) {
    block()
}

fun HTMLElement.replaceRender(
    scope: CoroutineScope,
    block: RenderContext.() -> Unit
): List<HTMLElement> {
    clear()
    return append {
        val context = DOMRenderContext(this, createElementScope(scope), this@replaceRender)
        context.block()
    }
}

fun HTMLElement.appendRender(
    scope: CoroutineScope,
    block: RenderContext.() -> Unit
) = append {
    val context = DOMRenderContext(this, createElementScope(scope), this@appendRender)
    context.block()
}

fun HTMLElement.prependRender(
    scope: CoroutineScope,
    block: RenderContext.() -> Unit
) = prepend {
    val context = DOMRenderContext(this, createElementScope(scope), this@prependRender)
    context.block()
}

fun Id.replaceRender(
    scope: CoroutineScope,
    block: RenderContext.() -> Unit
) = (document.body!!.querySelector(this) ?: error("element not found: $this")).replaceRender(scope, block)