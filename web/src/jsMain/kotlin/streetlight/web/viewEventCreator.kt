package streetlight.web

import koala.dom.RenderContext
import koala.dom.button
import koala.dom.column
import koala.dom.message
import koala.dom.paragraph
import koala.dom.row
import koala.dom.textField
import koala.html.label
import kotlinx.html.js.option
import kotlinx.html.js.select

fun RenderContext.viewEventCreator(app: AppContext) {
    val eventCreator = app.home.eventCreator

    column {
        row {
            label("Location")
            textField(eventCreator::setLocationName, eventCreator.stateFlow.mapDistinct { it.locationName })
            button("query") {
                eventCreator.queryLocation()
            }
        }
        textField(eventCreator::setEventName)
        select {
            name = "rum"
            option {
                value = "dark"
                +"Dark Rum"
            }
            option {
                value = "spiced"
                +"Spiced Rum"
            }
            option {
                value = "gold"
                +"Gold Rum"
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