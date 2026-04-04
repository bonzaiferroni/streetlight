package koala.dom

import koala.css.Class
import koala.html.Id
import kotlinx.browser.document
import org.w3c.dom.HTMLElement

fun RenderContext.wireBlock(
    elementId: Id,
    ancestor: HTMLElement? = null,
    wireOnView: Boolean = true,
    block: RenderContext.() -> Unit
): HTMLElement {
    val element = (ancestor ?: document.body)?.querySelector(elementId) ?: document.getElementOrNullById(elementId)
        ?: error("couldn't find ${elementId.identifier}")

    wireBlock(element, wireOnView, block)

    return element
}

fun RenderContext.wireBlocks(
    elementClass: Class,
    ancestor: HTMLElement? = null,
    wireOnView: Boolean = true,
    block: RenderContext.() -> Unit
): List<HTMLElement> {
    val elements = (ancestor ?: document.body)?.querySelectorAll(elementClass)
    ?: error("couldn't find $elementClass")

    elements.forEach {
        wireBlock(it, wireOnView, block)
    }

    return elements
}

fun RenderContext.wireBlock(
    element: HTMLElement,
    wireOnView: Boolean = true,
    block: RenderContext.() -> Unit
) {
    fun wireElement() {
        element.renderRoot(renderScope, block)
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