package koala.dom

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.html.js.p
import kotlinx.html.style

fun RenderContext.message(flow: Flow<UIMessage?>) {
    val element = p {
        style = "display: none;"
    }

    renderScope.launch {
        flow.collect { msg ->
            if (msg == null) {
                element.style.display = "none"
            } else {
                element.style.removeProperty("display")
                element.textContent = msg.message
            }
        }
    }
}