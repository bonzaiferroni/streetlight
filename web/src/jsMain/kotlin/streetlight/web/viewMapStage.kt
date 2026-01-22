package streetlight.web

import kotlinx.browser.window
import kotlinx.coroutines.flow.map
import react.FC
import react.Props
import react.RefCallback
import react.create
import web.dom.document
import react.dom.client.createRoot
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.h2
import react.dom.html.ReactHTML.input
import react.dom.html.ReactHTML.p
import react.useRef
import react.useState
import streetlight.web.renderElement
import web.cssom.CSSTransition
import web.cssom.ClassName
import web.dom.ElementId
import web.html.HTMLDivElement
import web.html.HTMLInputElement
import web.html.InputType
import web.html.checkbox
import web.html.text
import kotlin.collections.component1
import kotlin.collections.component2

fun viewMapStage(stage: MapStage) {
    val mount = createRoot(document.getElementById(ElementId("select-point"))!!)
    mount.render(MapStageView.create() { this.stage = stage } )
}

external interface MapStageProps: Props {
    var stage: MapStage
}

val MapStageView = FC<MapStageProps> { props ->
    val stage = props.stage
    p {
        +"Hello map!"
    }
    div {
        input {
            type = InputType.text
            onChange = { event ->
                stage.setState { it.copy(name = event.target.value) }
            }
        }
        input {
            val element = useRef<HTMLInputElement>(null)
            type = InputType.text
            ref = RefCallback { el ->
                element.current = el
                stage.stateFlow.map { "Hello ${it.name}!" }.collect {
                    element.current?.value = it
                }
            }
        }
        input {
            type = InputType.checkbox
        }
    }
    renderState(stage.stateFlow.map { it.queriedLocation }) {
        p {
            +"You are at ${it.lon}, ${it.lat}"
        }
    }
    renderState(stage.stateFlow.map { it.events } ) { allEvents ->
        allEvents.groupBy { it.eventType }.forEach { (eventType, events) ->
            div {
                className = ClassName("event-group")

                ref = RefCallback {
                    it.style.opacity = "1"
                }

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