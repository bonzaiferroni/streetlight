package streetlight.web

import kotlinx.browser.document
import kotlinx.coroutines.flow.map
import kotlinx.html.checkBoxInput
import kotlinx.html.id
import kotlinx.html.js.*
import kotlinx.html.textInput
import org.w3c.dom.HTMLDivElement

fun viewMapStage(stage: MapStage) {
    val mount = document.querySelector("#select-point") as HTMLDivElement
    mount.render {
        p {
            +"Hello map!"
        }
        div {
            textInput {
                id = "input-box"
                // onValueChange { value -> stage.setState { it.copy(name = value) } }
            }
            textInput {
                id = "output-box"
                // setValue(stage.stateFlow.map { "Hello ${it.name}!" })
            }
            checkBoxInput {
                onValueChange { console.log(it) }
            }
        }
        render(stage.stateFlow.map { it.queriedLocation }) {
            p {
                +"You are at ${it.lon}, ${it.lat}"
            }
        }
        render(stage.stateFlow.map { it.events } ) {
            console.log(it.joinToString(", ") { it.title })
        }
    }
}