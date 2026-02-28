package koala.dom

import koala.html.Id
import kotlinx.browser.document
import org.w3c.dom.HTMLElement

fun RenderContext.wireBlock(
    elementId: Id,
    ancestor: HTMLElement? = null,
    block: RenderContext.() -> Unit
): HTMLElement {
    val element = ancestor?.querySelector(elementId.selector) as? HTMLElement ?: document.getElementOrNullById(elementId)
        ?: error("couldn't find ${elementId.value}")
    var isRendered = false
    element.style.removeProperty("display")

    element.onView { isVisible ->
        if (isVisible && !isRendered) {
            isRendered = true
            element.renderRoot(renderScope, block)
        }
    }

    return element
}