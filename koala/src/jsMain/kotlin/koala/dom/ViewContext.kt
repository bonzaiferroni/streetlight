package koala.dom

import org.w3c.dom.HTMLElement

class ViewContext<out T>(
    val model: T,
    context: RenderContext
): RenderContext by context {
}

fun <T> RenderContext.viewContextOf(model: T, block: ViewContext<T>.() -> Unit) = ViewContext(model, this).block()

fun <T> ViewContext<T>.appendRender(element: HTMLElement, block: ViewContext<T>.() -> Unit) {
    element.appendRender(renderScope) {
        viewContextOf(model, block)
    }
}

fun <T> ViewContext<T>.prependRender(element: HTMLElement, block: ViewContext<T>.() -> Unit) {
    element.prependRender(renderScope) {
        viewContextOf(model, block)
    }
}

fun <T> ViewContext<T>.replaceRender(element: HTMLElement, block: ViewContext<T>.() -> Unit) {
    element.replaceRender(renderScope) {
        viewContextOf(model, block)
    }
}