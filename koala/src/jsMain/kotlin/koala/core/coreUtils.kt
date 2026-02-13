package koala.core

import koala.css.Modifier
import koala.html.Id
import kotlinx.browser.document
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import org.w3c.dom.asList

fun Element.queryAll(modifier: Modifier) = querySelectorAll(modifier.selector).asList()

fun Element.queryFirstOrNull(modifier: Modifier) = querySelector(modifier.selector) as? HTMLElement

fun Element.appendDiv(id: Id? = null): HTMLElement {
    val element = document.createElement("div") as HTMLElement
    if (id != null) {
        element.id = id.value
    }
    append(element)
    return element
}