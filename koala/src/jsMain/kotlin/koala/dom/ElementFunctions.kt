package koala.dom

import koala.css.Modifier
import koala.external.ScrollIntoViewOptions
import kotlinx.browser.window
import kotlinx.dom.addClass
import kotlinx.dom.removeClass
import org.w3c.dom.Element
import org.w3c.dom.Node

fun Element.unmodify(vararg modifier: Modifier) = modifier.forEach { classList.remove(it.value) }
fun Element.modify(vararg modifier: Modifier) = modifier.forEach { classList.add(it.value) }
fun Element.unmodify(modifiers: Collection<Modifier>) = modifiers.forEach { classList.remove(it.value) }
fun Element.modify(modifiers: Collection<Modifier>) = modifiers.forEach { classList.add(it.value) }

fun Node.modify(vararg modifier: Modifier) {
    val element = this as? Element ?: error("not an element")
    element.modify(*modifier)
}

fun Node.unmodify(vararg modifier: Modifier) {
    val element = this as? Element ?: error("not an element")
    element.unmodify(*modifier)
}


private const val MAX_ATTEMPTS = 30

fun Element.scrollWhenPresent(
    options: ScrollIntoViewOptions? = ScrollIntoViewOptions(
        behavior = "smooth",
        block = "nearest"
    )
) {
    var attempts = 0

    fun tryScroll() {
        if (isConnected) {
            scrollIntoView(options)
            return
        }

        if (++attempts >= MAX_ATTEMPTS) return

        window.requestAnimationFrame { tryScroll() }
    }

    window.requestAnimationFrame { tryScroll() }
}