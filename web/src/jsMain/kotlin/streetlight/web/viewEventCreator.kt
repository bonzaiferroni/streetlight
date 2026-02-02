package streetlight.web

import koala.css.*
import koala.dom.*
import koala.html.heading2
import kotlinx.html.js.p

fun RenderContext.viewEventCreator(app: AppContext) {
    val eventCreator = app.home.eventCreator

    column {
        column(QueryRow, FlexItems1, Width100) {
            card {
                heading2("what")
                textField(eventCreator::setEventName)
                dropMenu(eventCreator::setEventType) { it.label }
                flowBlock(
                    flow = eventCreator.stateFlow.mapDistinct { it.eventType.label },
                    modifiers = modify(Slide),
                    animate = true
                ) {
                    p {
                        +it
                    }
                }
            }
            card {
                heading2("where")
                textField(eventCreator::setLocationName, eventCreator.stateFlow.mapDistinct { it.locationName })
                button("query") {
                    eventCreator.queryLocation()
                }
            }
        }
        message(eventCreator.stateFlow.mapDistinct { it.message })
        row {
            button("cancel") {
                eventCreator.toggle()
            }
            button("create") {
                eventCreator.createEvent()
            }
        }
    }
}