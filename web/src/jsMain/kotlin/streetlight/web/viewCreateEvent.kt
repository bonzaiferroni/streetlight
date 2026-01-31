package streetlight.web

import koala.dom.button
import koala.dom.column
import koala.dom.row
import koala.html.paragraph
import kotlinx.datetime.Clock
import streetlight.model.data.EventType
import streetlight.model.data.LocationId
import streetlight.model.data.NewEvent

fun RenderContext.viewCreateEvent() {
    column {
        paragraph("create event")
        row {
            button("cancel") {
                portal.goBack()
            }
            button("create") {
                home.eventMap.createEvent(NewEvent(
                    locationId = LocationId.random(),
                    title = "Community Meeting",
                    startsAt = Clock.System.now(),
                    eventType = EventType.Fellowship
                    ))
                portal.goBack()
            }
        }
    }
}