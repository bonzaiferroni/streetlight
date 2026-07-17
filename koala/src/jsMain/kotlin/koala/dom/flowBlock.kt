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
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.dom.clear
import kotlinx.html.DIV
import kotlinx.html.js.div
import org.w3c.dom.HTMLDivElement
import kotlin.time.Duration.Companion.milliseconds

fun <State> ViewScope.flowBlock(
    flow: Flow<State>,
    modifiers: ModifierSet? = null,
    name: String = "flowBlock",
    cacheElements: Boolean = false,
    config: (DIV.() -> Unit)? = null,
    onTransition: ((State) -> Unit)? = null,
    block: ViewScope.(State) -> Unit
): HTMLDivElement {
    val magic = modifiers?.contains(Magic) ?: false
    val element = div {
        addModifiers(FlowBlockKey.Class, modifiers)
        config?.invoke(this)
    }

    var render: RenderJob? = null
    var renderedOnce = false
    val cache = mutableMapOf<State, RenderJob>()

    launchEffect("$name > launchEffect") {
        var currentValue: State? = null
        flow.collect { value ->
            if (renderedOnce && value == currentValue) return@collect
            renderedOnce = true
            if (!cacheElements) render?.job?.cancel()

            fun appendRender() {
                element.clear()
                render = cache[value]?.also {
                    it.elements.forEach { child ->
                        element.append(child)
                    }
                } ?: createRenderJob(name, element, value, block)

                if (cacheElements) {
                    cache[value] = render
                }
                currentValue = value
            }

            if (magic) {
                val interval = KoalaTheme.MAGIC_INTERVAL.milliseconds
                launch("$name > render") {
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
                try {
                    appendRender()
                    onTransition?.invoke(value)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Throwable) {
                    launch("$name > render") { throw e }
                }
            }
        }
    }

    return element
}

val defaultMagic = modify(Magic, Blur, SlideLeft)

