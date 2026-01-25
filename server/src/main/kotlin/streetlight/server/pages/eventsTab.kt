package streetlight.server.pages

import koala.html.AlignItemsCenter
import koala.html.card
import koala.html.column
import koala.html.label
import kotlinx.html.FlowContent
import kotlinx.html.a
import streetlight.model.data.Event

fun FlowContent.eventsTab(
    events: List<Event>
) {
    column(AlignItemsCenter) {
        events.forEach { event ->
            a {
                href = "/event-portal/${event.eventId.value}"
                card {
                    label(event.title)
                }
            }
        }
        homeFooter()
    }
}