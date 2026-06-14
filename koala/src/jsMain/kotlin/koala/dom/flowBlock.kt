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
import kotlinx.dom.clear
import kotlinx.html.DIV
import kotlinx.html.classes
import kotlinx.html.js.div
import org.w3c.dom.HTMLDivElement

fun <State> AppScope.flowBlock(
    flow: Flow<State>,
    modifiers: ModifierSet? = null,
    cacheElements: Boolean = false,
    config: (DIV.() -> Unit)? = null,
    onTransition: ((State) -> Unit)? = null,
    block: AppScope.(State) -> Unit
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

    launchEffect {
        var currentValue: State? = null
        flow.collect { value ->
            // do we need renderedOnce?
            if (renderedOnce && value == currentValue) return@collect
            renderedOnce = true
            if (cacheElements) render?.job?.cancel()
            currentValue = value

            fun appendRender() {
                element.clear()
                render = cache[value]?.also {
                    it.elements.forEach { child ->
                        element.append(child)
                    }
                } ?: createRenderJob(element, value, block)

                if (cacheElements) {
                    cache[value] = render
                    val key = cache.entries.firstOrNull()?.key
                    cache.remove(key)
                }
            }

            if (magic) {
                val interval = KoalaTheme.MAGIC_INTERVAL.toLong()
                launch {
                    if (render != null) {
                        element.modify(Transitioning).unmodifyAfterFrame(Reveal)
                        delay(interval)
                    }
                    appendRender()
                    element.modify(Reveal)
                    onTransition?.invoke(value)
                    delay(interval)
                    element.unmodify(Transitioning)
                }
            } else {
                appendRender()
                onTransition?.invoke(value)
            }
        }
    }

    return element
}

val defaultMagic = modify(Magic, Blur, SlideLeft)

