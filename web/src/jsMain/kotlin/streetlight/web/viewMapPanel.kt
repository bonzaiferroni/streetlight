package streetlight.web

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.html.checkBoxInput
import kotlinx.html.div
import kotlinx.html.h2
import kotlinx.html.js.span
import kotlinx.html.p
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.time.Duration.Companion.seconds

fun RenderContext.viewMapPanel() {
    val eventMap = app.home.eventMap

    mountRender("select-point") {
        p {
            +"Hello map!"
        }
        div {
            textField(
                onChangeValue = eventMap::setName
            )
            textField(
                binding = eventMap.stateFlow.map { "Hello ${it.name}!" }
            )
            checkBoxInput {
                // onValueChange { console.log(it) }
            }
        }
        renderState(eventMap.stateFlow.map { it.queriedLocation }) {
            p {
                +"You are at ${it.lng}, ${it.lat}"
            }
            p {
                +"And you've been there "
                val span = span {
                    +"0"
                }
                +" seconds"

                renderScope.launch {
                    var seconds = 0
                    while (true) {
                        delay(1.seconds)
                        seconds++
                        span.textContent = seconds.toString()
                    }
                }
            }
        }
        renderState(eventMap.stateFlow.map { it.events }, true) { allEvents ->
            allEvents.groupBy { it.eventType }.forEach { (eventType, events) ->
                div("event-group") {
                    h2 {
                        +eventType.label
                    }
                    events.forEach { event ->
                        p {
                            +event.title
                        }
                    }
                }.style.opacity = "1"
            }
        }
    }
}