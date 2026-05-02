package koala.dom

import org.w3c.dom.DOMRectReadOnly
import org.w3c.dom.Element

external class ResizeObserver(
    callback: (Array<ResizeObserverEntry>, ResizeObserver) -> Unit
) {
    fun observe(target: Element)
    fun observe(target: Element, options: ResizeObserverOptions)
    fun unobserve(target: Element)
    fun disconnect()
}

external interface ResizeObserverEntry {
    val target: Element
    val contentRect: DOMRectReadOnly
    val borderBoxSize: Array<ResizeObserverSize>
    val contentBoxSize: Array<ResizeObserverSize>
}

external interface ResizeObserverSize {
    val blockSize: Double
    val inlineSize: Double
}

external interface ResizeObserverOptions {
    var box: String? // "content-box", "border-box", or "device-pixel-content-box"
}