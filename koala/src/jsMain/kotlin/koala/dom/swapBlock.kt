package koala.dom

import koala.css.Magic
import koala.css.Reveal
import koala.css.Property
import koala.html.Id
import koala.html.Queryable
import kotlinx.browser.window
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.css.Display
import org.w3c.dom.HTMLElement
import org.w3c.dom.asList

fun AppScope.wireSwapBlock(
    element: HTMLElement,
    bindFlow: (Flow<Id>)? = null,
) {
    bindFlow?.let { flow ->
        val children = element.children.asList().map { it as HTMLElement }
        console.log(element.id)
        val isMagic = element.classList.contains(Magic.identifier)
        parentScope.launch {
            var isInitial = true
            flow.collect { id ->
                if (isMagic) {
                    children.setReveal(id, isInitial)
                    isInitial = false
                } else {
                    children.setVisibility(id)
                }
            }
        }
    }
}

fun AppScope.queryAndWireSwapBlock(
    ancestor: HTMLElement,
    queryable: Queryable,
    bindFlow: (Flow<Id>)? = null,
) {
    val element = ancestor.querySelector(queryable)
        ?: error("swap block not found: ${queryable.selector}")
    wireSwapBlock(element, bindFlow)
}

private fun List<HTMLElement>.setVisibility(id: Id) {
    forEach { child ->
        when (child.id == id.identifier) {
            true -> child.style.removeStyle(Property.Display)
            else -> child.style.setStyle(Property.Display.to(Display.none))
        }
    }
}

private fun List<HTMLElement>.setReveal(id: Id, isInitial: Boolean) {
    forEach { child ->
        when (child.id == id.identifier) {
            true -> {
                child.style.removeStyle(Property.Display)
                if (isInitial) {
                    // pops into view on the first switch, this doesn't seem to help
                    window.requestAnimationFrame {
                        child.modify(Reveal)
                    }
                } else {
                    child.modify(Reveal)
                }
            }
            else -> {
                child.unmodify(Reveal)
            }
        }
    }
}