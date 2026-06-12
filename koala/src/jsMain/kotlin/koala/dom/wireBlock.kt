package koala.dom

import koala.html.Id
import kotlinx.browser.document
import org.w3c.dom.HTMLElement

fun RenderScope.wireBlock(
    elementId: Id,
    ancestor: HTMLElement? = null,
    wireOnView: Boolean = true,
    block: RenderScope.() -> Unit
): HTMLElement {
    val element = (ancestor ?: document.body)?.querySelector(elementId) ?: document.getElementOrNullById(elementId)
        ?: error("couldn't find ${elementId.identifier}")

    wireBlock(element, wireOnView, block)

    return element
}

fun RenderScope.wireBlock(
    element: HTMLElement,
    wireOnView: Boolean = true,
    block: RenderScope.() -> Unit
) {
    fun wireElement() {
        replaceRender(element, block)
    }

    if (wireOnView) {
        var isRendered = false
        element.style.removeProperty("display")

        element.onView { isVisible ->
            if (isVisible && !isRendered) {
                isRendered = true
                wireElement()
            }
        }
    } else {
        wireElement()
    }
}