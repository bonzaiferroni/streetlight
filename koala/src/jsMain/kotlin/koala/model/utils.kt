package koala.model

import koala.css.ModifierSet
import koala.dom.modify
import org.w3c.dom.Document
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLImageElement
import kotlin.js.json

@Deprecated("use external interface")
fun jsObject(block: dynamic.() -> Unit): dynamic {
    val obj = js("{}")
    block(obj)
    return obj
}

fun Document.createDiv(modifiers: ModifierSet? = null): HTMLDivElement {
    val element = createElement("div") as HTMLDivElement
    modifiers?.let {
        element.modify(it)
    }
    return element
}

