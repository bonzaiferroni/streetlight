package koala.dom

import koala.css.Modifier
import kotlinx.dom.addClass
import kotlinx.dom.removeClass
import org.w3c.dom.Element
import org.w3c.dom.Node

fun Element.unmodify(vararg modifier: Modifier) = modifier.forEach { classList.remove(it.value) }
fun Element.modify(vararg modifier: Modifier) = modifier.forEach { classList.add(it.value) }

fun Node.modify(vararg modifier: Modifier) {
    val element = this as? Element ?: error("not an element")
    element.modify(*modifier)
}

fun Node.unmodify(vararg modifier: Modifier) {
    val element = this as? Element ?: error("not an element")
    element.unmodify(*modifier)
}
