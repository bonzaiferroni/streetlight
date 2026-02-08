package koala.dom

import koala.css.Animate
import koala.css.ElementClass
import koala.css.ModifierSet
import koala.css.Reveal
import koala.css.applyModifiers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.dom.clear
import kotlinx.html.DIV
import kotlinx.html.classes
import kotlinx.html.dom.append
import kotlinx.html.js.div
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement

fun <State> RenderContext.flowBlock(
    flow: Flow<State>,
    modifiers: ModifierSet? = null,
    animate: Boolean = false,
    cacheRenderedElements: Boolean = false,
    config: (DIV.() -> Unit)? = null,
    block: RenderContext.(State) -> Unit
): HTMLDivElement {
    val element = div {
        applyModifiers(ElementClass.flowBlock, modifiers)
        if (animate) {
            classes += Animate.value
        }
        config?.invoke(this)
    }

    var render: RenderCache? = null
    var job: Job? = null
    var renderedOnce = false
    val renderCaches = mutableMapOf<State, RenderCache>()

    renderScope.launch {
        var currentValue: State? = null
        flow.collect { value ->
            // do we need renderedOnce?
            if (renderedOnce && value == currentValue) return@collect
            renderedOnce = true
            if (!cacheRenderedElements) job?.cancel()
            currentValue = value

            job = SupervisorJob()
            val localScope = CoroutineScope(Dispatchers.Main + job)

            fun createRender(): RenderCache {
                var context: RenderContext
                val elements =element.append {
                    context = RenderContext(this, localScope)
                    context.block(value)
                }
                return RenderCache(context, elements)
            }

            fun appendRender() {
                render = renderCaches[value]?.also {
                    it.elements.forEach { child ->
                        element.append(child)
                    }
                } ?: createRender()
                render.context.emitOnLoad()
                if (cacheRenderedElements) renderCaches[value] = render
            }

            if (animate) {
                element.unmodify(Reveal)
                val exitJob = renderScope.launch {
                    if (render == null) return@launch
                    element.unmodify(Reveal)
                    // element.modify(Hide)
                    delay(200)
                    element.clear()
                }
                localScope.launch {
                    exitJob.join()
                    appendRender()
                    // element.unmodify(Hide)
                    element.modify(Reveal)
                }
            } else {
                element.clear()
                appendRender()
            }
        }
    }

    return element
}

private class RenderCache(
    val context: RenderContext,
    val elements: List<HTMLElement>
)