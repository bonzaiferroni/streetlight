package koala.dom

@Deprecated("pass the model as an argument")
class ViewContext<out T>(
    val model: T,
    context: ViewScope
): ViewScope by context {
}

@Deprecated("pass the model as an argument")
fun <T> ViewScope.viewContextOf(model: T, block: ViewContext<T>.() -> Unit) = ViewContext(model, this).block()