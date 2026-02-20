package koala.dom

import koala.dom.getElementById
import koala.html.AppRoute
import koala.html.GeoMapSelector
import koala.html.Id
import kotlinx.browser.document
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.dom.clear
import kotlinx.html.dom.append
import org.w3c.dom.HTMLElement

open class RenderContext(
    consumer: DOMContext,
    val renderScope: CoroutineScope,
): DOMContext by consumer

fun HTMLElement.renderRoot(
    scope: CoroutineScope,
    block: RenderContext.() -> Unit
) {
    clear()
    append {
        val context = RenderContext(this, scope)
        context.block()
    }
}

fun RenderContext.mountRender(
    elementId: Id,
    block: RenderContext.() -> Unit
) {
    val mount = document.getElementById(elementId)
    mount.renderRoot(renderScope, block)
}

fun RenderContext.mountRenderOnView(
    elementId: Id,
    block: RenderContext.() -> Unit
) {
    val element = document.getElementById(elementId)
    var isRendered = false

    element.onView { isVisible ->
        if (isVisible && !isRendered) {
            isRendered = true
            element.renderRoot(renderScope, block)
        }
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

fun <T> createRender(parent: HTMLElement, scope: CoroutineScope, value: T, block: RenderContext.(T) -> Unit): RenderCache {
    val job = SupervisorJob()
    val localScope = CoroutineScope(scope.coroutineContext + job)
    var context: RenderContext
    val elements = parent.append {
        context = RenderContext(this, localScope)
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