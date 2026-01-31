package streetlight.web

import koala.dom.DOMContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.dom.addClass
import kotlinx.dom.clear
import kotlinx.dom.removeClass
import kotlinx.html.INPUT
import kotlinx.html.InputType
import kotlinx.html.dom.append
import kotlinx.html.*
import kotlinx.html.js.onInputFunction
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLInputElement

class RenderContext(
    consumer: DOMContext,
    val renderScope: CoroutineScope,
    val app: AppContext,
): DOMContext by consumer, AppContext by app {

    fun <State> renderState(
        flow: Flow<State>,
        animate: Boolean = false,
        configureParent: (DIV.() -> Unit)? = null,
        configureContainer: (DIV.() -> Unit)? = null,
        cacheRenderedElements: Boolean = false,
        block: RenderContext.(State) -> Unit
    ) {
        val parent = div("state-render") {
            configureParent?.invoke(this)
        }
        var render: HTMLElement? = null
        var job: Job? = null
        var renderedOnce = false
        val renderCache = mutableMapOf<State, HTMLElement>()

        renderScope.launch {
            var currentValue: State? = null
            flow.collect {  value ->
                if (renderedOnce && value == currentValue) return@collect
                renderedOnce = true
                if (!cacheRenderedElements) job?.cancel()
                currentValue = value

                render?.hide(animate)

                render = renderCache[value]?.also {
                    parent.append(it)
                } ?: parent.append {
                    val container = div() {
                        configureContainer?.invoke(this)
                    }
                    container.append {
                        job = SupervisorJob()
                        val scope = CoroutineScope(Dispatchers.Main + job)
                        RenderContext(this, scope, app).block(value)
                    }
                    if (cacheRenderedElements) renderCache[value] = container
                }.first()

                render.show(animate)

                if (animate) {
                    val height = render.scrollHeight
                    parent.style.height = "${height}px"
                }
            }
        }
    }

    fun HTMLElement.hide(animate: Boolean) {
        if (animate) {
            addClass("exit-stage")
            removeClass("enter-stage")
            renderScope.launch {
                delay(250)
                remove()
                removeClass("exit-stage")
            }
        } else {
            remove()
        }
    }

    fun HTMLElement.show(animate: Boolean) {
        if (animate) {
            addClass("state-render-animation")
            renderScope.launch {
                delay(200)
                addClass("enter-stage")
            }
        }
    }
}

fun HTMLElement.renderRoot(
    scope: CoroutineScope,
    app: AppContext,
    block: RenderContext.() -> Unit
) {
    clear()
    append {
        RenderContext(this, scope, app).block()
    }
}

fun RenderContext.textField(
    onChangeValue: ((String) -> Unit)? = null,
    binding: Flow<String>? = null,
    block: (INPUT.() -> Unit)? = null
) {
    val element = input {
        type = InputType.text
        onChangeValue?.let { callback ->
            onInputFunction = {
                val v = (it.target as HTMLInputElement).value
                callback(v)
            }
        }
        block?.invoke(this)
    } as HTMLInputElement

    binding?.let {
        renderScope.launch {
            binding.distinctUntilChanged().collect {
                element.value = it
            }
        }
    }
}