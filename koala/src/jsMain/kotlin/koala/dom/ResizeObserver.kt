package koala.dom

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
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

fun Element.resizeFlow(): Flow<DOMRectReadOnly> = callbackFlow {
    val observer = ResizeObserver { entries, _ ->
        trySend(entries[0].contentRect)
    }
    observer.observe(this@resizeFlow)
    awaitClose { observer.disconnect(); console.log("goodbye") }
}