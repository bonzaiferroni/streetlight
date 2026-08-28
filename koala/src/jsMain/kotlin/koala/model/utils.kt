package koala.model

import koala.css.ModifierSet
import koala.dom.modify
import web.dom.Document
import web.html.HTMLDivElement
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

