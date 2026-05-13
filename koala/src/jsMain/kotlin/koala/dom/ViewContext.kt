package koala.dom

import koala.html.Id
import kotlinx.browser.document
import org.w3c.dom.HTMLElement

@Deprecated("pass the model as an argument")
class ViewContext<out T>(
    val model: T,
    context: RenderContext
): RenderContext by context {
}

@Deprecated("pass the model as an argument")
fun <T> RenderContext.viewContextOf(model: T, block: ViewContext<T>.() -> Unit) = ViewContext(model, this).block()