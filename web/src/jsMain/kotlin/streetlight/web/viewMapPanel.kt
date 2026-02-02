package streetlight.web

import koala.css.Css
import koala.dom.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.html.checkBoxInput
import kotlinx.html.div
import kotlinx.html.h2
import kotlinx.html.id
import kotlinx.html.js.span
import kotlinx.html.p
import streetlight.model.data.NewLocation
import kotlin.time.Duration.Companion.seconds

fun RenderContext.viewMapPanel(app: AppContext) {
    val eventMap = app.home.eventMap
    val eventCreator = app.home.eventCreator
    val gateAgent = app.gateAgent

    mountRender("map-panel") {
        renderState(eventCreator.stateFlow.mapDistinct { it.isCreatingEvent }, true) { isCreatingEvent ->
            if (isCreatingEvent) {
                viewEventCreator(app)
            } else {
                column {
                    button("location query") {
                        renderScope.launch {
                            // val info = app.client.location.readPlaceInfo(eventMap.stateNow.center)
                            // console.log(jsonPrettyConfig.encodeToString(info))
                            val locations = app.client.location.queryLocation(eventMap.stateNow.center)
                            console.log(locations?.joinToString(", ") { it.name })
                        }
                    }
                    button("create location") {
                        renderScope.launch {
                            val point = eventMap.stateNow.center
                            val info = app.client.location.readPlaceInfo(point)
                            console.log(info)
                            val id = app.client.location.createLocation(NewLocation(
                                name = info.name.takeIf { it.isNotBlank() } ?: info.address.road ?: info.addressType,
                                geoPoint = point
                            ))
                            console.log(id?.value)
                        }
                    }
                    renderState(eventMap.focusFlow) { (location, event) ->
                        column {
                            textBlock("Event: ${event?.title}")
                            textBlock("Location: ${location?.name}")
                            button("Add Event") {
                                gateAgent.checkIn {
                                    eventCreator.toggle()
                                }
                            }
                        }
                    }
                    renderState(eventMap.stateFlow.map { it.areaEvents }, true) { allEvents ->
                        box(Css("map-event-panel")) {
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