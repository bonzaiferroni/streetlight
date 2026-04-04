package koala.dom

import koala.html.Id
import kotlinx.browser.document
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.dom.clear
import kotlinx.html.dom.append
import org.w3c.dom.HTMLElement

interface RenderContext: DOMContext {
    val renderScope: CoroutineScope
}

class DOMRenderContext(
    consumer: DOMContext,
    override val renderScope: CoroutineScope,
): RenderContext, DOMContext by consumer

fun HTMLElement.renderRoot(
    scope: CoroutineScope,
    block: RenderContext.() -> Unit
) {
    clear()
    append {
        val context = DOMRenderContext(this, scope)
        context.block()
    }
}

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
        context = DOMRenderContext(this, localScope)
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