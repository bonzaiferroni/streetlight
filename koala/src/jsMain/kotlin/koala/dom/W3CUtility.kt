package koala.dom

import kotlinx.dom.clear
import kotlinx.html.dom.append
import kotlinx.html.dom.prepend
import org.w3c.dom.HTMLElement as W3CElementCore
import org.w3c.dom.HTMLDivElement as W3CDivElement
import org.w3c.dom.HTMLSpanElement as W3CSpanElement
import org.w3c.dom.HTMLInputElement as W3CInputElement
import org.w3c.dom.HTMLButtonElement as W3CButtonElement
import org.w3c.dom.HTMLImageElement as W3CImageElement
import web.html.*

typealias W3CElement = W3CElementCore

fun HTMLElement.asW3C() = unsafeCast<W3CElement>()
fun W3CElement.asWeb() = unsafeCast<HTMLElement>()

fun HTMLElement.clear() = asW3C().clear()
fun HTMLElement.append(block: AppendScope.() -> Unit) = asW3C().append {
    block()
}.asWeb()
fun HTMLElement.prepend(block: AppendScope.() -> Unit) = asW3C().prepend {
    block()
}.asWeb()

fun List<W3CElement>.asWeb() = unsafeCast<List<HTMLElement>>()

fun W3CDivElement.asWeb() = unsafeCast<HTMLDivElement>()
fun W3CSpanElement.asWeb() = unsafeCast<HTMLSpanElement>()
fun W3CInputElement.asWeb() = unsafeCast<HTMLInputElement>()
fun W3CButtonElement.asWeb() = unsafeCast<HTMLButtonElement>()
fun W3CImageElement.asWeb() = unsafeCast<HTMLImageElement>()