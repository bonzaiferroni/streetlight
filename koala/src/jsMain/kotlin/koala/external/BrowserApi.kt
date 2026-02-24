package koala.external

import kotlinx.js.JsPlainObject
import org.w3c.dom.Element

@JsPlainObject
external interface ScrollIntoViewOptions {
    val behavior: String? // "auto" | "smooth"
    val block: String?    // "start" | "center" | "end" | "nearest"
    val inline: String?   // same as block, but horizontal
}