package koala.dom

import koala.css.Blur
import koala.css.Magic
import koala.css.ModifierSet
import koala.css.Reveal
import koala.css.SlideLeft
import koala.css.addModifiers
import koala.css.modify
import koala.html.FlowBlockKey
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.dom.clear
import kotlinx.html.DIV
import kotlinx.html.classes
import kotlinx.html.js.div
import org.w3c.dom.HTMLDivElement

fun <State> RenderContext.flowBlock(
    flow: Flow<State>,
    modifiers: ModifierSet? = null,
    renderCacheCount: Int? = null,
    config: (DIV.() -> Unit)? = null,
    onTransition: ((State) -> Unit)? = null,
    block: RenderContext.(State) -> Unit
): HTMLDivElement {
    val magic = modifiers?.contains(Magic) ?: false
    val element = div {
        addModifiers(FlowBlockKey.Class, modifiers)
        if (magic) {
            classes += Magic.identifier
        }
        config?.invoke(this)
    }

    var render: RenderCache? = null
    var renderedOnce = false
    val cache = mutableMapOf<State, RenderCache>()

    renderScope.launch {
        var currentValue: State? = null
        flow.collect { value ->
            // do we need renderedOnce?
            if (renderedOnce && value == currentValue) return@collect
            renderedOnce = true
            if (renderCacheCount == null) render?.job?.cancel()
            currentValue = value

            fun appendRender() {
                render = cache[value]?.also {
                    it.elements.forEach { child ->
                        element.append(child)
                    }
                } ?: createRender(element, renderScope, value, block)
                if (renderCacheCount != null) {
                    cache[value] = render
                    if (renderCacheCount > 0 && cache.size > renderCacheCount) {
                        val key = cache.entries.firstOrNull()?.key
                        cache.remove(key)
                    }
                }
            }

            if (magic) {
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

val defaultMagic = modify(Magic, Blur, SlideLeft)

