package koala.dom

import koala.modifier.Clickable
import koala.modifier.className
import web.dom.Element
import web.events.Event
import web.events.EventType
import web.events.addEventListener
import web.pointer.CLICK
import web.pointer.PointerEvent

/** Calls [onEvent] on each [event]. */
fun <T: Event> Element.onEvent(event: EventType<T>, onEvent: (Event) -> Unit) {
    addEventListener(event, onEvent)
}

//@Suppress("UNCHECKED_CAST")
//fun <T> Element.onCustomEvent(event: CustomElementEvent<T>, onEvent: (T) -> Unit) {
//    addEventListener(event.label, {
//        val event = it as CustomEvent
//        onEvent(event.detail as T)
//    })
//}
//
//fun <T> Element.sendCustomEvent(event: CustomElementEvent<T>, value: T) {
//    val event = CustomEvent(event.label, CustomEventInit(value))
//    dispatchEvent(event)
//}

/** Calls [block] with the event on each click, and marks the element clickable. */
fun <T: Element> T.onClickEvent(block: (Event) -> Unit): T {
    onEvent(PointerEvent.CLICK, block)
    classList.add(Clickable.className)
    return this
}

/** Calls [block] with the element on each click, and marks the element clickable. */
fun <T: Element> T.onClickElement(block: (T) -> Unit): T {
    onEvent(PointerEvent.CLICK) {
        block(this)
    }
    classList.add(Clickable.className)
    return this
}

/** Calls [block] on each click, and marks the element clickable. */
fun <T: Element> T.onClick(block: () -> Unit): T {
    onEvent(PointerEvent.CLICK) {
        block()
    }
    classList.add(Clickable.className)
    return this
}