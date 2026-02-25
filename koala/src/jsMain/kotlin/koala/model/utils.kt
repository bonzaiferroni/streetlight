package koala.model

import org.w3c.dom.Document
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLImageElement
import kotlin.js.json

fun jsObject(block: dynamic.() -> Unit): dynamic {
    val obj = js("{}")
    block(obj)
    return obj
}

fun jsObject(vararg pairs: Pair<String, Any?>) = json(*pairs)

fun Document.createDiv() = createElement("div") as HTMLElement
fun Document.createImg(src: String? = null) = createElement("img").also { element ->
    src?.let {
        element.setAttribute("src", it)
    }
} as HTMLImageElement

