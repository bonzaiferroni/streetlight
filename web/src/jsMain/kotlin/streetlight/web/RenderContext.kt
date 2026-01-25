package streetlight.web

import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.dom.addClass
import kotlinx.dom.clear
import kotlinx.dom.hasClass
import kotlinx.dom.removeClass
import kotlinx.html.INPUT
import kotlinx.html.InputType
import kotlinx.html.TagConsumer
import kotlinx.html.dom.append
import kotlinx.html.*
import kotlinx.html.js.onInputFunction
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLInputElement

class RenderContext(
    val consumer: TagConsumer<HTMLElement>,
    val renderScope: CoroutineScope,
    val app: AppContext,
): TagConsumer<HTMLElement> by consumer {

    fun <T> renderState(
        flow: Flow<T>,
        animate: Boolean = false,
        block: RenderContext.(T) -> Unit
    ) {
        val parent = consumer.div("state-render")
        var lastContainer: HTMLElement? = null
        var job: Job? = null

        renderScope.launch {
            var currentValue: T? = null
            flow.collect {  value ->
                if (value == currentValue) return@collect
                job?.cancel()
                job = SupervisorJob()
                currentValue = value

                if (animate) lastContainer?.exitStage()
                else lastContainer?.remove()

                val scope = CoroutineScope(Dispatchers.Main + job)
                parent.append {
                    lastContainer = div()
                    lastContainer.append {
                        RenderContext(this, scope, app).block(value)
                    }
                    if (animate) lastContainer.addClass("state-render-animation")
                }
                if (animate) {
                    val height = lastContainer?.scrollHeight ?: 0
                    parent.style.height = "${height}px"
                    lastContainer?.enterStage()
                }
            }
        }
    }

    fun mountRender(
        elementId: String,
        block: RenderContext.() -> Unit
    ) {
        val mount = document.getElementById(elementId) as HTMLElement
        mount.renderRoot(renderScope, app, block)
    }
}

fun HTMLElement.enterStage() {
    window.setTimeout({
        if (!hasClass("exit-stage")) {
            addClass("enter-stage")
        }
    }, 200)
}

fun HTMLElement.exitStage() {
    addClass("exit-stage")
    removeClass("enter-stage")
    window.setTimeout({
        remove()
    }, 250)
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