package streetlight.web

import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.html.checkBoxInput
import kotlinx.html.div
import kotlinx.html.dom.append
import kotlinx.html.h2
import kotlinx.html.id
import kotlinx.html.js.onLoadFunction
import kotlinx.html.js.span
import kotlinx.html.onVolumeChange
import kotlinx.html.p
import kotlinx.html.textInput
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import org.w3c.dom.MutationObserver
import org.w3c.dom.MutationObserverInit
import org.w3c.dom.asList
import org.w3c.dom.events.Event
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.time.Duration.Companion.seconds

fun viewMapStage(stage: MapStage) {
    val mount = document.getElementById("select-point") as HTMLElement
    mount.renderRoot {
        p {
            +"Hello map!"
        }
        div {
            textField(
                onChangeValue = { value -> stage.setState { it.copy(name = value) } }
            )
            textField(
                binding = stage.stateFlow.map { "Hello ${it.name}!" }
            )
            checkBoxInput {
                // onValueChange { console.log(it) }
            }
        }
        renderState(stage.stateFlow.map { it.queriedLocation }) {
            p {
                +"You are at ${it.lon}, ${it.lat}"
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
        renderState(stage.stateFlow.map { it.events }, true) { allEvents ->
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