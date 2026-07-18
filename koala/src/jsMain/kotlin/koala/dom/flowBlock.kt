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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.html.DIV
import kotlinx.html.js.div
import org.w3c.dom.HTMLDivElement
import kotlin.time.Duration.Companion.milliseconds

fun <State> ViewScope.flowBlock(
    flow: Flow<State>,
    modifiers: ModifierSet? = null,
    name: String = "flowBlock",
    config: (DIV.() -> Unit)? = null,
    onTransition: ((State) -> Unit)? = null,
    block: ViewScope.(State) -> Unit
): HTMLDivElement {
    val magic = modifiers?.contains(Magic) ?: false
    val element = div {
        addModifiers(FlowBlockKey.Class, modifiers)
        config?.invoke(this)
    }

    var view: View? = null
    var renderedOnce = false
    var launchJob: Job? = null

    launchEffect("$name > launchEffect") {
        var currentValue: State? = null
        flow.collect { value ->
            if (renderedOnce && value == currentValue) return@collect
            renderedOnce = true
            view?.dispose()

            fun mountView() {
                view = this@flowBlock.mountChildView(name, element) {
                    block(value)
                }
                currentValue = value
            }

            if (magic) {
                val interval = KoalaTheme.MAGIC_INTERVAL.milliseconds
                launchJob?.cancel()
                launchJob = launch("$name > mountView") {
                    if (view != null) {
                        element.modify(Transitioning).unmodifyAfterFrame(Reveal)
                        delay(interval)
                    }
                    mountView()
                    element.modify(Reveal)
                    onTransition?.invoke(value)
                    delay(interval)
                    element.unmodify(Transitioning)
                }
            } else {
                try {
                    mountView()
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

