package koala.dom

import koala.html.Id
import kotlinx.browser.document
import kotlinx.coroutines.CoroutineScope
import org.w3c.dom.HTMLElement

fun RenderContext.wireBlock(
    elementId: Id,
    ancestor: HTMLElement? = null,
    wireOnView: Boolean = true,
    scope: CoroutineScope = renderScope,
    block: RenderContext.() -> Unit
): HTMLElement {
    val element = (ancestor ?: document.body)?.querySelector(elementId) ?: document.getElementOrNullById(elementId)
        ?: error("couldn't find ${elementId.identifier}")

    wireBlock(element, wireOnView, scope, block)

    return element
}

fun RenderContext.wireBlock(
    element: HTMLElement,
    wireOnView: Boolean = true,
    scope: CoroutineScope = renderScope,
    block: RenderContext.() -> Unit
) {
    fun wireElement() {
        element.replaceRender(scope, block)
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