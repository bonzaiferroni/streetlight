package streetlight.web

import kotlinx.browser.window
import kotlinx.coroutines.flow.map
import kotlinx.html.Entities
import kotlinx.html.*
import kotlinx.html.js.onLoadFunction
import org.w3c.dom.HTMLElement
import kotlin.invoke

fun HTMLElement.renderLegacy(stage: MapStage) {
    renderElement {
        p {
            +"Hello map!"
        }
        div {
            textInput {
                id = "input-box"
                onValueChange { value -> stage.setState { it.copy(name = value) } }
            }
            textInput {
                id = "output-box"
                setValue(stage.stateFlow.map { "Hello ${it.name}!" })
            }
//            FC {
//                val (value, setValue) = useState("")
//                textInput {
//                    val elRef = useRef(null)
//                    RefCallback<Unit> {
//
//                    }
//                    +"Ahoy"
//                }
//                input {
//                    type = InputType.text
//                    this.value = value
//
//                    onChangeFunction = { e ->
//                        val target = e.target as HTMLInputElement
//                        setValue(target.value)
//                    }
//                }
//            }
            checkBoxInput {
                onValueChange { console.log(it) }
            }
        }
        renderState(stage.stateFlow.map { it.queriedLocation }) {
            p {
                +"You are at ${it.lon}, ${it.lat}"
            }
        }
        renderState(stage.stateFlow.map { it.events } ) { allEvents ->
            allEvents.groupBy { it.eventType }.forEach { (eventType, events) ->
                div("event-group") {
                    onLoadFunction = { event ->
                        console.log("ey")
                        window.requestAnimationFrame {
                            event.element?.style?.opacity = "1"
                        }
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
}