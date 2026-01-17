package streetlight.web

import org.w3c.dom.Document
import org.w3c.dom.HTMLElement

fun jsObject(block: dynamic.() -> Unit): dynamic {
    val obj = js("{}")
    block(obj)
    return obj
}

fun Document.createDiv() = createElement("div") as HTMLElement