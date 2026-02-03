package streetlight.web

import koala.css.Blur
import koala.css.Css
import koala.css.modify
import koala.dom.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.html.checkBoxInput
import kotlinx.html.div
import kotlinx.html.id
import kotlinx.html.js.span
import kotlinx.html.p
import kotlin.time.Duration.Companion.seconds

fun RenderContext.viewMapPanel(app: AppContext) {
    val eventMap = app.home.streetMap
    val eventCreator = app.home.eventCreator
    val gateAgent = app.gateAgent

    mountRender("map-panel") {
        flowBlock(
            flow = eventCreator.stateFlow.mapDistinct { it.isCreatingEvent },
            modifiers = modify(Blur),
            animate = true
        ) { isCreatingEvent ->
            if (isCreatingEvent) {
                viewEventCreator(app)
            } else {
                column {
                    viewMapConfig(app)

                    viewMapCards(app)

                    // sandbox
                    // viewSandbox(app)
                }
            }
        }
    }
}

object MapPanel {
    val container = Css("map-event-panel")
    val card = Css("map-panel-card")
}

fun RenderContext.testInput(app: AppContext) {
    val eventMap = app.home.streetMap

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
    val eventMap = app.home.streetMap

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