package koala.dom

import koala.utils.EventHandler
import koala.utils.MutableEventHandler
import kotlinx.browser.document
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
    val renderScope: CoroutineScope
): DOMContext by consumer {
    private val onLoadHandler = MutableEventHandler()
    val onLoad: EventHandler = onLoadHandler

    internal fun emitOnLoad() {
        onLoadHandler.emit()
    }
}

fun HTMLElement.renderRoot(
    scope: CoroutineScope,
    block: RenderContext.() -> Unit
) {
    clear()
    append {
        RenderContext(this, scope).block()
    }
}

fun RenderContext.mountRender(
    elementId: String,
    block: RenderContext.() -> Unit
) {
    val mount = document.getElementById(elementId) as HTMLElement
    mount.renderRoot(renderScope, block)
}