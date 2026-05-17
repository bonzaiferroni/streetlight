package koala.dom

import koala.css.Blur
import koala.css.KoalaTheme
import koala.css.Magic
import koala.css.ModifierSet
import koala.css.Reveal
import koala.css.SlideLeft
import koala.css.Transitioning
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

    var render: RenderJob? = null
    var renderedOnce = false
    val cache = mutableMapOf<State, RenderJob>()

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
                } ?: createRenderJob(element, value, block)

                if (renderCacheCount != null) {
                    cache[value] = render
                    if (renderCacheCount > 0 && cache.size > renderCacheCount) {
                        val key = cache.entries.firstOrNull()?.key
                        cache.remove(key)
                    }
                }
            }

            if (magic) {
                val interval = KoalaTheme.MAGIC_INTERVAL.toLong()
                renderScope.launch {
                    if (render != null) {
                        element.modify(Transitioning).unmodifyAfterFrame(Reveal)
                        delay(interval)
                        element.clear()
                    }
                    appendRender()
                    element.modify(Reveal)
                    onTransition?.invoke(value)
                    delay(interval)
                    element.unmodify(Transitioning)
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

