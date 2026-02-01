package streetlight.web

import koala.css.Css
import koala.dom.RenderContext
import koala.dom.box
import koala.dom.button
import koala.dom.card
import koala.dom.column
import koala.dom.row
import koala.dom.textField
import koala.html.paragraph
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.html.checkBoxInput
import kotlinx.html.div
import kotlinx.html.h2
import kotlinx.html.id
import kotlinx.html.js.span
import kotlinx.html.p
import kotlin.time.Duration.Companion.seconds

fun AppContext.viewMapPanel() {
    val eventMap = home.eventMap

    mountRender("select-point") {
        column {
            renderState(eventMap.placeFlow) { (location, event) ->
                if (event != null) {
                    row {
                        paragraph("Event: ${event.title}")
                    }
                } else if (location != null) {
                    row {
                        paragraph("Location: ${location.name}")
                        button("Add Event") {
                            gateAgent.checkIn {
                                portal.go(CreateEventRoute)
                            }
                        }
                    }
                } else {
                    button("Add Location") {
                        gateAgent.checkIn {
                            portal.go(CreateLocationRoute)
                        }
                    }
                }
            }

            renderState(eventMap.stateFlow.map { it.events }, true) { allEvents ->
                box(Css("map-event-panel")) {
                    p {
                        +"eyyyyy!"
                    }
                    allEvents.groupBy { it.eventType }.forEach { (eventType, events) ->
                        card(Css("map-event-group")) {
                            h2 {
                                +eventType.label
                            }
                            events.forEach { event ->
                                p {
                                    +event.title
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun RenderContext.testInput(app: AppContext) {
    val eventMap = app.home.eventMap

    p {
        +"Hello map!"
    }
    div {
        id = "test-form"
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
}

fun RenderContext.showLocation(app: AppContext) {
    val eventMap = app.home.eventMap

    renderState(eventMap.stateFlow.map { it.queriedBounds }) {
        val center = it.center
        p {
            +"You are at ${center.lng}, ${center.lat}"
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
}