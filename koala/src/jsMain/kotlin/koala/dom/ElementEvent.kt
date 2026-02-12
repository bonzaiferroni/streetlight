package koala.dom

import koala.css.Clickable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import org.w3c.dom.Element
import org.w3c.dom.events.Event

interface ElementEvent {
    val value: String
}

object OnClick: ElementEvent { override val value = "click"}

fun Element.onClickEvent(block: (Event) -> Unit) {
    addEventListener("click", block)
    modify(Clickable)
}

fun Element.onClick(block: () -> Unit) {
    addEventListener("click", {
        block()
    })
    modify(Clickable)
}

fun Element.addEventListener(event: ElementEvent, block: (Event) -> Unit) {
    addEventListener(event.value, block)
}

fun Element.removeEventListener(event: ElementEvent, block: (Event) -> Unit) {
    removeEventListener(event.value, block)
}

fun Element.eventFlow(event: ElementEvent, scope: CoroutineScope): Flow<Event> {
    val flow = MutableSharedFlow<Event>()
    addEventListener(event.value, {
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

    addEventListener(event.value, listener)

    awaitClose {
        removeEventListener(event.value, listener)
    }
}