package koala.dom

import koala.modifier.*
import kotlinx.dom.clear
import kotlinx.html.DIV
import kotlinx.html.dom.append
import kotlinx.html.dom.create
import kotlinx.html.dom.prepend
import kotlinx.html.js.div
import web.dom.Document
import kotlinx.browser.document as W3CDocument
import org.w3c.dom.HTMLElement as W3CElementCore
import org.w3c.dom.HTMLDivElement as W3CDivElement
import org.w3c.dom.HTMLSpanElement as W3CSpanElement
import org.w3c.dom.HTMLInputElement as W3CInputElement
import org.w3c.dom.HTMLButtonElement as W3CButtonElement
import org.w3c.dom.HTMLImageElement as W3CImageElement
import web.html.*

/** The DOM element type of kotlinx.html, which the Kotlin wrappers' element types are cast to and from. */
typealias W3CElement = W3CElementCore

fun HTMLElement.asW3C() = unsafeCast<W3CElement>()
fun W3CElement.asWeb() = unsafeCast<HTMLElement>()

fun HTMLElement.clear() = asW3C().clear()
/** Appends what [block] builds to this element's children. */
fun HTMLElement.append(block: AppendScope.() -> Unit) = asW3C().append {
    block()
}.asWeb()
/** Prepends what [block] builds before this element's children. */
fun HTMLElement.prepend(block: AppendScope.() -> Unit) = asW3C().prepend {
    block()
}.asWeb()

fun List<W3CElement>.asWeb() = unsafeCast<List<HTMLElement>>()

fun W3CDivElement.asWeb() = unsafeCast<HTMLDivElement>()
fun W3CSpanElement.asWeb() = unsafeCast<HTMLSpanElement>()
fun W3CInputElement.asWeb() = unsafeCast<HTMLInputElement>()
fun W3CButtonElement.asWeb() = unsafeCast<HTMLButtonElement>()
fun W3CImageElement.asWeb() = unsafeCast<HTMLImageElement>()

/** A new `div`, not yet in the document. */
fun Document.createDiv(
    mod: Modifier? = null,
    config: DIV.() -> Unit = { },
) = W3CDocument.create.div {
    addModifiers(mod)
    config()
}.asWeb()