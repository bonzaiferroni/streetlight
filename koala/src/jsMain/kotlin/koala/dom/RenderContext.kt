package koala.dom

import koala.html.Id
import kotlinx.browser.document
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.dom.addClass
import kotlinx.dom.clear
import kotlinx.dom.removeClass
import kotlinx.html.INPUT
import kotlinx.html.InputType
import kotlinx.html.dom.append
import kotlinx.html.*
import kotlinx.html.js.onInputFunction
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLInputElement

class RenderContext(
    consumer: DOMContext,
    val renderScope: CoroutineScope
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

class RenderCache(
    val context: RenderContext,
    val job: Job,
    val localScope: CoroutineScope,
    val elements: List<HTMLElement>
) {
    val firstElement get() = elements.first()
}

fun <T> createRender(parent: HTMLElement, value: T, block: RenderContext.(T) -> Unit): RenderCache {
    val job = SupervisorJob()
    val localScope = CoroutineScope(Dispatchers.Main + job)
    var context: RenderContext
    val elements = parent.append {
        context = RenderContext(this, localScope)
        context.block(value)
    }
    return RenderCache(context, job, localScope, elements)
}

fun createRender(parent: HTMLElement, block: RenderContext.() -> Unit) = createRender(parent, Unit) {
    block()
}