package streetlight.web

import koala.css.FlexItems1
import koala.css.QueryRow
import koala.css.Width100
import koala.dom.RenderContext
import koala.dom.button
import koala.dom.card
import koala.dom.column
import koala.dom.dropMenu
import koala.dom.message
import koala.dom.row
import koala.dom.flowBlock
import koala.dom.textField
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
                flowBlock(eventCreator.stateFlow.mapDistinct { it.eventType.label }, animate = true) {
                    p {
                        +it
                    }
                }
                // textBlock(eventCreator.stateFlow.mapDistinct { it.eventType.label })
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

// <select name="rum">
//    <option value="dark">Dark Rum</option>
//    <option value="spiced">Spiced Rum</option>
//    <option value="gold">Gold Rum</option>
//</select>