package koala.dom

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.html.js.p
import org.w3c.dom.HTMLParagraphElement

fun DOMContext.textBlock(text: String) = p {
    +text
}

fun RenderContext.textBlock(flow: Flow<String>): HTMLParagraphElement {
    val element = p { }
    renderScope.launch {
        flow.distinctUntilChanged().collect {
            element.textContent = it
        }
    }
    return element
}