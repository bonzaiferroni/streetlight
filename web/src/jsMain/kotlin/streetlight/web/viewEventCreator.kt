package streetlight.web

import koala.css.QueryRow
import koala.dom.RenderContext
import koala.dom.button
import koala.dom.card
import koala.dom.column
import koala.dom.dropMenu
import koala.dom.message
import koala.dom.row
import koala.dom.textBlock
import koala.dom.textField
import koala.html.heading2
import streetlight.model.data.EventType

fun RenderContext.viewEventCreator(app: AppContext) {
    val eventCreator = app.home.eventCreator

    column {
        column(QueryRow) {
            card {
                heading2("what")
                textField(eventCreator::setEventName)
                dropMenu(eventCreator::setEventType) { it.label }
                textBlock(eventCreator.stateFlow.mapDistinct { it.eventType.label })
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