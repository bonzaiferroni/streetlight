package koala.dom

import koala.css.KoalaTheme
import koala.css.Magic
import koala.css.Reveal
import koala.css.StyleProperty
import koala.html.Id
import koala.html.Queryable
import kotlinx.browser.window
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.css.Display
import org.w3c.dom.HTMLElement
import org.w3c.dom.asList
import kotlin.time.Duration.Companion.seconds

fun RenderContext.wireSwapBlock(
    element: HTMLElement,
    bindFlow: (Flow<Id>)? = null,
) {
    bindFlow?.let { flow ->
        val children = element.children.asList().map { it as HTMLElement }
        val isMagic = element.classList.contains(Magic.value)
        renderScope.launch {
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

fun RenderContext.queryAndWireSwapBlock(
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
        when (child.id == id.value) {
            true -> child.style.removeProperty(StyleProperty.display)
            else -> child.style.setProperty(StyleProperty.display.to(Display.none))
        }
    }
}

private fun List<HTMLElement>.setReveal(id: Id, isInitial: Boolean) {
    forEach { child ->
        when (child.id == id.value) {
            true -> {
                child.style.removeProperty(StyleProperty.display)
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