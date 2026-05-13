package koala.dom

import koala.html.Id
import kotlinx.browser.document
import org.w3c.dom.HTMLElement

class ViewContext<out T>(
    val model: T,
    context: RenderContext
): RenderContext by context {
}

fun <T> RenderContext.viewContextOf(model: T, block: ViewContext<T>.() -> Unit) = ViewContext(model, this).block()

fun <T> ViewContext<T>.appendView(element: HTMLElement, block: ViewContext<T>.() -> Unit) {
    appendRender(element) {
        viewContextOf(model, block)
    }
}

fun <T> ViewContext<T>.prependView(element: HTMLElement, block: ViewContext<T>.() -> Unit) {
    prependRender(element) {
        viewContextOf(model, block)
    }
}

fun <T> ViewContext<T>.replaceView(element: HTMLElement, block: ViewContext<T>.() -> Unit) {
    replaceRender(element) {
        viewContextOf(model, block)
    }
}

fun <T> ViewContext<T>.replaceView(id: Id, ancestor: HTMLElement? = null, block: ViewContext<T>.() -> Unit) {
    val element = (ancestor ?: document.body!!).querySelector(id) ?: error("element not found: $id")
    replaceRender(element) {
        viewContextOf(model, block)
    }
}