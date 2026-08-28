package koala.dom

import koala.css.Clickable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import web.dom.Element
import web.events.CustomEvent
import web.events.CustomEventInit
import web.events.Event
import web.events.EventType
import web.events.addEventListener
import web.pointer.CLICK
import web.pointer.PointerEvent

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

fun <T: Element> T.onClickEvent(block: (Event) -> Unit): T {
    onEvent(PointerEvent.CLICK, block)
    modify(Clickable)
    return this
}

fun <T: Element> T.onClickElement(block: (T) -> Unit): T {
    onEvent(PointerEvent.CLICK) {
        block(this)
    }
    modify(Clickable)
    return this
}

fun <T: Element> T.onClick(block: () -> Unit): T {
    onEvent(PointerEvent.CLICK) {
        block()
    }
    modify(Clickable)
    return this
}