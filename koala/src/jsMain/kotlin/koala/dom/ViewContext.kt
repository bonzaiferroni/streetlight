package koala.dom

class ViewContext<T>(
    val model: T,
    context: RenderContext
): RenderContext by context {
}

fun <T> RenderContext.viewContextOf(model: T, block: ViewContext<T>.() -> Unit) = ViewContext(model, this).block()