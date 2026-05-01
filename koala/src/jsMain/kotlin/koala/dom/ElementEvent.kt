package koala.dom

import koala.css.Clickable
import koala.html.CustomElementEvent
import koala.html.ElementEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import org.w3c.dom.CustomEvent
import org.w3c.dom.CustomEventInit
import org.w3c.dom.Element
import org.w3c.dom.events.Event

fun Element.onEvent(event: ElementEvent, onEvent: (Event) -> Unit) {
    addEventListener(event.label, onEvent)
}

@Suppress("UNCHECKED_CAST")
fun <T> Element.onCustomEvent(event: CustomElementEvent<T>, onEvent: (T) -> Unit) {
    addEventListener(event.label, {
        val event = it as CustomEvent
        onEvent(event.detail as T)
    })
}

fun <T> Element.sendCustomEvent(event: CustomElementEvent<T>, value: T) {
    val event = CustomEvent(event.label, CustomEventInit(value))
    dispatchEvent(event)
}

fun <T: Element> T.onClickEvent(block: (Event) -> Unit): T {
    onEvent(ElementEvent.onClick, block)
    modify(Clickable)
    return this
}

@Suppress("UNCHECKED_CAST")
fun <T: Element> T.onClickElement(block: (T) -> Unit): T {
    onEvent(ElementEvent.onClick) {
        block(it.target as T)
    }
    modify(Clickable)
    return this
}

fun <T: Element> T.onClick(block: () -> Unit): T {
    onEvent(ElementEvent.onClick) {
        block()
    }
    modify(Clickable)
    return this
}