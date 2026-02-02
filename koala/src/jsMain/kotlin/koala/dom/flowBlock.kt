package koala.dom

import koala.css.CssClass
import koala.css.StateBlock
import koala.css.modify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.dom.addClass
import kotlinx.dom.clear
import kotlinx.dom.removeClass
import kotlinx.html.DIV
import kotlinx.html.classes
import kotlinx.html.dom.append
import kotlinx.html.js.div
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement

fun <State> RenderContext.flowBlock(
    flow: Flow<State>,
    vararg modifiers: CssClass,
    animate: Boolean = false,
    cacheRenderedElements: Boolean = false,
    config: (DIV.() -> Unit)? = null,
    block: RenderContext.(State) -> Unit
): HTMLDivElement {
    val element = div {
        modify(StateBlock, *modifiers)
        if (animate) {
            classes += "animate"
        }
        config?.invoke(this)
    }

    var render: List<HTMLElement>? = null
    var job: Job? = null
    var renderedOnce = false
    val renderCache = mutableMapOf<State, List<HTMLElement>>()

    renderScope.launch {
        var currentValue: State? = null
        flow.collect { value ->
            if (renderedOnce && value == currentValue) return@collect
            renderedOnce = true
            if (!cacheRenderedElements) job?.cancel()
            currentValue = value

            job = SupervisorJob()
            val localScope = CoroutineScope(Dispatchers.Main + job)

            fun appendRender() {
                render = renderCache[value]?.also {
                    it.forEach { child ->
                        element.append(child)
                    }
                } ?: element.append {
                    RenderContext(this, localScope).block(value)
                }
                if (cacheRenderedElements) renderCache[value] = render
            }

            if (animate) {
                element.removeClass("reveal")
                val exitJob = renderScope.launch {
                    if (render == null) return@launch
                    delay(200)
                    element.clear()
                }
                localScope.launch {
                    exitJob.join()
                    appendRender()
                    element.addClass("reveal")
                }
            } else {
                element.clear()
                appendRender()
            }
        }
    }

    return element
}