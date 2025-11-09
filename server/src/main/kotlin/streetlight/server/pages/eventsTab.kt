package streetlight.server.pages

import klutch.html.card
import klutch.html.column
import klutch.html.label
import kotlinx.html.FlowContent
import kotlinx.html.HTML
import kotlinx.html.a
import streetlight.model.data.Event

fun FlowContent.eventsTab(
    events: List<Event>
) {
    column {
        events.forEach { event ->
            a {
                href = "/event-portal/${event.eventId.value}"
                card {
                    label(event.title)
                }
            }
        }
    }
}