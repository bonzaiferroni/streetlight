package koala.dom

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.dom.addClass
import kotlinx.dom.removeClass
import kotlinx.html.DIV
import kotlinx.html.div
import kotlinx.html.dom.append
import org.w3c.dom.HTMLElement
import kotlin.collections.first

@Deprecated("use flowBlock")
fun <State> RenderContext.renderState(
    flow: Flow<State>,
    animate: Boolean = false,
    configureParent: (DIV.() -> Unit)? = null,
    configureContainer: (DIV.() -> Unit)? = null,
    cacheRenderedElements: Boolean = false,
    block: RenderContext.(State) -> Unit
) {
    val parent = div("state-render") {
        configureParent?.invoke(this)
    }
    var render: HTMLElement? = null
    var job: Job? = null
    var renderedOnce = false
    val renderCache = mutableMapOf<State, HTMLElement>()

    renderScope.launch {
        var currentValue: State? = null
        flow.collect {  value ->
            if (renderedOnce && value == currentValue) return@collect
            renderedOnce = true
            if (!cacheRenderedElements) job?.cancel()
            currentValue = value

            render?.let {
                hide(it, animate)
            }

            render = renderCache[value]?.also {
                parent.append(it)
            } ?: parent.append {
                val container = div() {
                    configureContainer?.invoke(this)
                }
                container.append {
                    job = SupervisorJob()
                    val scope = CoroutineScope(Dispatchers.Main + job)
                    RenderContext(this, scope).block(value)
                }
                if (cacheRenderedElements) renderCache[value] = container
            }.first()

            show(render, animate)

//                if (animate) {
//                    val height = render.scrollHeight
//                    parent.style.height = "${height}px"
//                }
        }
    }
}

private fun RenderContext.hide(element: HTMLElement, animate: Boolean) {
    if (animate) {
        element.addClass("exit-stage")
        element.removeClass("enter-stage")
        renderScope.launch {
            delay(250)
            element.remove()
            element.removeClass("exit-stage")
        }
    } else {
        element.remove()
    }
}

private fun RenderContext.show(element: HTMLElement, animate: Boolean) {
    if (animate) {
        element.addClass("state-render-animation")
        renderScope.launch {
            delay(200)
            element.addClass("enter-stage")
        }
    }
}