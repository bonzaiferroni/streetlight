package koala.dom

import kotlinx.js.JsPlainObject
import org.w3c.dom.Element

external class IntersectionObserver {
    constructor(
        callback: (Array<IntersectionObserverEntry>, IntersectionObserver) -> Unit
    )
    constructor(
        callback: (Array<IntersectionObserverEntry>, IntersectionObserver) -> Unit,
        options: IntersectionObserverInit
    )

    fun observe(target: Element)
    fun unobserve(target: Element)
    fun disconnect()
}

@JsPlainObject
external interface IntersectionObserverEntry {
    val target: Element
    val isIntersecting: Boolean
    val intersectionRatio: Double
}

@JsPlainObject
external interface IntersectionObserverInit {
    var root: Element?
    var rootMargin: String?
    var threshold: dynamic
}

fun Element.onView(block: (Boolean) -> Unit) {
    val observer = IntersectionObserver { entries, _ ->
        entries.forEach { entry ->
            block(entry.isIntersecting)
        }
    }

    observer.observe(this)
}