package koala.dom

import koala.modifier.Blur
import koala.modifier.Magic
import koala.modifier.MagicStyle
import koala.modifier.Reveal
import koala.modifier.SlideLeft
import koala.modifier.Transitioning
import koala.modifier.addModifiers
import koala.modifier.modify
import koala.html.FlowBlockStyle
import kampfire.model.Tap
import koala.modifier.Modifier
import koala.modifier.contains
import koala.modifier.unmodify
import koala.modifier.unmodifyAfterFrame
import koala.utils.launch
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.html.DIV
import kotlinx.html.js.div
import web.html.HTMLDivElement
import kotlin.time.Duration.Companion.milliseconds

fun <Value> ViewScope.flowBlock(
    initialValue: Value,
    flow: Flow<Value>,
    mod: Modifier? = null,
    name: String = "flowBlock",
    config: (DIV.() -> Unit)? = null,
    onTransition: ((Value) -> Unit)? = null,
    block: ViewScope.(Value) -> Unit
): HTMLDivElement {
    val magic = mod?.contains(Magic) ?: false
    val element = div {
        addModifiers(mod, FlowBlockStyle.Class)
        config?.invoke(this)
    }.asWeb()

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
                val interval = MagicStyle.Interval.milliseconds
                launchJob?.cancel()
                launchJob = launch("$name > transition") {
                    if (view != null) {
                        element.modify(Transitioning).unmodifyAfterFrame(Reveal)
                        delay(interval)
                    }
                    tryMountView(value)
                    delay(interval)
                    element.unmodify(Transitioning)
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
    mod: Modifier? = null,
    name: String = "flowBlock",
    config: (DIV.() -> Unit)? = null,
    onTransition: ((Value) -> Unit)? = null,
    block: ViewScope.(Value) -> Unit
) = flowBlock(
    initialValue = tap.now,
    flow = tap.flow,
    mod = mod,
    name = name,
    config = config,
    onTransition = onTransition,
    block = block
)

