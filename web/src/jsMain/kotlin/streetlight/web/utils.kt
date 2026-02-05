package streetlight.web

import org.w3c.dom.Document
import org.w3c.dom.HTMLElement
import kotlin.js.json

fun jsObject(block: dynamic.() -> Unit): dynamic {
    val obj = js("{}")
    block(obj)
    return obj
}

fun jsObject(vararg pairs: Pair<String, Any?>) = json(*pairs)

fun Document.createDiv() = createElement("div") as HTMLElement

