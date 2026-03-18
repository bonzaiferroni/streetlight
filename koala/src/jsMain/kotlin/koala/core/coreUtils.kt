package koala.core

import koala.css.Clickable
import koala.css.Modifier
import koala.dom.modify
import koala.html.Attribute
import koala.html.Id
import kotlinx.browser.document
import kotlinx.html.impl.DelegatingMap
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import org.w3c.dom.NamedNodeMap
import org.w3c.dom.asList

fun Document.queryAll(modifier: Modifier) = querySelectorAll(modifier.selector).asList()

fun Document.onClickElementAll(modifier: Modifier, onClick: (HTMLElement) -> Unit) = querySelectorAll(modifier.selector).asList()
    .forEach {
        val element = it as HTMLElement
        element.modify(Clickable)
        element.addEventListener("click", {
            onClick(element)
        })
    }

fun Document.onClick(id: Id, onClick: () -> Unit) = querySelector(id.value)?.let {
    val element = it as HTMLElement
    element.modify(Clickable)
    element.addEventListener("click", {
        onClick()
    })
}

fun Document.queryAttribute(attribute: Attribute) = querySelector(attribute.selector)?.attributes?.get(attribute)

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

operator fun NamedNodeMap.get(attribute: Attribute): String? = this.getNamedItem(attribute.key)?.value