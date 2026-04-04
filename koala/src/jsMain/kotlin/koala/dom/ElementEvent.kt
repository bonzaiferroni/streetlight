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

fun Element.onClickEvent(block: (Event) -> Unit) {
    onEvent(ElementEvent.onClick, block)
    modify(Clickable)
}

fun Element.onClick(block: () -> Unit) {
    onEvent(ElementEvent.onClick, {
        block()
    })
    modify(Clickable)
}

fun Element.removeEventListener(event: ElementEvent, block: (Event) -> Unit) {
    removeEventListener(event.label, block)
}

fun Element.eventFlow(event: ElementEvent, scope: CoroutineScope): Flow<Event> {
    val flow = MutableSharedFlow<Event>()
    addEventListener(event.label, {
        scope.launch {
            flow.emit(it)
        }
    })
    return flow
}

fun Element.eventFlow(event: ElementEvent): Flow<Event> = callbackFlow {
    val listener: (Event) -> Unit = { e ->
        trySend(e).isSuccess
    }

    addEventListener(event.label, listener)

    awaitClose {
        removeEventListener(event.label, listener)
    }
}