package streetlight.web

import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.dom.clear
import kotlinx.html.INPUT
import kotlinx.html.TagConsumer
import kotlinx.html.dom.append
import kotlinx.html.id
import kotlinx.html.js.div
import kotlinx.html.js.onInputFunction
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLInputElement

class RenderContext(
    val renderConsumer: TagConsumer<HTMLElement>,
    val job: Job
): TagConsumer<HTMLElement> by renderConsumer {
    fun <T> render(flow: Flow<T>, block: RenderContext.(T) -> Unit) {
        val element = renderConsumer.div() {
            +"Test123"
        }
        CoroutineScope(Dispatchers.Main + job).launch {
            var currentValue: T? = null
            flow.collect {  value ->
                if (value == currentValue) return@collect
                currentValue = value
                element.clear()
                element.render {
                    block(value)
                }
            }
        }
    }

    fun INPUT.onValueChange(block: (String) -> Unit) {
        onInputFunction = {
            val v = (it.target as HTMLInputElement).value
            block(v)
        }
    }

    fun <T> INPUT.setValue(flow: Flow<T>) {
        var element: HTMLInputElement? = null
        CoroutineScope(Dispatchers.Main + job).launch {
            flow.distinctUntilChanged().collect {
                if (element == null) element = window.document.getElementById(id) as? HTMLInputElement
                element?.value = it.toString()
            }
        }
    }
}

fun HTMLElement.render(block: RenderContext.() -> Unit) {
    append {
        RenderContext(this, Job()).block()
    }
}