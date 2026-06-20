package koala.dom

import koala.css.*
import koala.html.SwapStyle
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.html.dom.append
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement

fun TagScope.swap(
    mod: ModifierSet? = null,
    content: TagScope.(SwapElement) -> Unit,
): SwapElement {
    val parent = div(modify(SwapStyle.Class, mod))
    val element = SwapElement(parent)

    val children = parent.append {
        content(element)
    }

    element.init(children)

    return element
}

class SwapElement(
    val parent: HTMLDivElement,
) {
    lateinit var children: List<HTMLElement>
        private set

    var current: HTMLElement? = null
        private set

    var currentIndex: Int? = null
        private set

    internal fun init(children: List<HTMLElement>) {
        this.children = children
        children.firstOrNull()?.let { setCurrent(it) }
    }

    fun setCurrent(element: HTMLElement) {
        val index = children.indexOf(element)
        require(index >= 0) { "must be a child of parent" }
        parent.setProperty(Property.ViewTransitionName.to("swap"))
        val transition = document.startViewTransition {
            current?.unmodify(Reveal)
            element.modify(Reveal)
            current = element
            currentIndex = index
        }
        transition.finished.then {
            parent.removeProperty(Property.ViewTransitionName)
        }
    }

    fun next() {
        val index = currentIndex?.takeIf { it + 1 < children.size }?.let { it + 1 } ?: 0
        val element = children.getOrNull(index) ?: error("element not found")
        setCurrent(element)
    }
}