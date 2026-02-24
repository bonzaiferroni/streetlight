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
    onTransition: ((State) -> Unit)? = null,
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
    var renderedOnce = false
    val renderCaches = mutableMapOf<State, RenderCache>()

    renderScope.launch {
        var currentValue: State? = null
        flow.collect { value ->
            // do we need renderedOnce?
            if (renderedOnce && value == currentValue) return@collect
            renderedOnce = true
            if (!cacheRenderedElements) render?.job?.cancel()
            currentValue = value

            fun appendRender() {
                render = renderCaches[value]?.also {
                    it.elements.forEach { child ->
                        element.append(child)
                    }
                } ?: createRender(element, renderScope, value, block)
                if (cacheRenderedElements) renderCaches[value] = render
            }

            if (animate) {
                renderScope.launch {
                    if (render != null) {
                        element.unmodify(Reveal)
                        delay(200)
                        element.clear()
                    }
                    appendRender()
                    element.modify(Reveal)
                    onTransition?.invoke(value)
                }
            } else {
                element.clear()
                appendRender()
                onTransition?.invoke(value)
            }
        }
    }

    return element
}