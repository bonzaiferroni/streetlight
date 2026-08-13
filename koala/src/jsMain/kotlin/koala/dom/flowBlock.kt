package koala.dom

import koala.css.Blur
import koala.css.Dummy
import koala.css.KoalaTheme
import koala.css.Magic
import koala.css.ModifierSet
import koala.css.Reveal
import koala.css.SlideLeft
import koala.css.Transitioning
import koala.css.addModifiers
import koala.css.modify
import koala.html.FlowBlockKey
import koala.model.Tap
import koala.utils.launch
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.html.DIV
import kotlinx.html.js.div
import org.w3c.dom.HTMLDivElement
import kotlin.time.Duration.Companion.milliseconds

fun <Value> ViewScope.flowBlock(
    initialValue: Value,
    flow: Flow<Value>,
    modifiers: ModifierSet? = null,
    name: String = "flowBlock",
    config: (DIV.() -> Unit)? = null,
    onTransition: ((Value) -> Unit)? = null,
    block: ViewScope.(Value) -> Unit
): HTMLDivElement {
    val magic = modifiers?.contains(Magic) ?: false
    val element = div {
        addModifiers(modifiers, FlowBlockKey.Class, if (magic) Transitioning else null)
        config?.invoke(this)
    }

    var view: View? = null
    var launchJob: Job? = null

    fun mountView(value: Value) {
        view = this@flowBlock.mountChildView(name, element) {
            block(value)
        }
        if (magic) {
            element.modify(Reveal)
        }
    }

    fun tryMountView(value: Value) {
        try {
            mountView(value)
            onTransition?.invoke(value)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            scope.launch("$name > mountView") { throw e }
        }
    }

    tryMountView(initialValue)

    launchEffect("$name > launchEffect") {
        var isFirstEmission = true
        flow.collect { value ->
            val isReplay = isFirstEmission && value == initialValue && view != null
            isFirstEmission = false
            if (isReplay) return@collect
            view?.dispose()

            if (magic) {
                val interval = KoalaTheme.MagicInterval.milliseconds
                launchJob?.cancel()
                launchJob = launch("$name > transition") {
                    if (view != null) {
                        element.modify(Dummy).unmodifyAfterFrame(Reveal)
                        delay(interval)
                    }
                    tryMountView(value)
                    delay(interval)
                    element.unmodify(Dummy)
                }
            } else {
                tryMountView(value)
            }
        }
    }

    return element
}

fun <Value> ViewScope.flowBlock(
    tap: Tap<Value>,
    modifiers: ModifierSet? = null,
    name: String = "flowBlock",
    config: (DIV.() -> Unit)? = null,
    onTransition: ((Value) -> Unit)? = null,
    block: ViewScope.(Value) -> Unit
) = flowBlock(
    initialValue = tap.now,
    flow = tap.flow,
    modifiers = modifiers,
    name = name,
    config = config,
    onTransition = onTransition,
    block = block
)

val defaultMagic = modify(Magic, Blur, SlideLeft)

