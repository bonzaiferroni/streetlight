package koala.dom

import koala.html.Id
import web.dom.document
import web.html.HTMLElement

fun ViewScope.wireBlock(
    elementId: Id,
    ancestor: HTMLElement? = null,
    wireOnView: Boolean = true,
    block: ViewScope.() -> Unit
): HTMLElement {
    val element = (ancestor ?: document.body).querySelector(elementId) ?: document.getElementOrNullById(elementId)
        ?: error("couldn't find ${elementId.identifier}")

    wireBlock(elementId.identifier, element, wireOnView, block)

    return element
}

fun ViewScope.wireBlock(
    name: String,
    element: HTMLElement,
    wireOnView: Boolean = true,
    block: ViewScope.() -> Unit
) {
    fun wireElement() {
        mountChildView(name, element, block)
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