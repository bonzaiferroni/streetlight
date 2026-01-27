package streetlight.server.pages

import koala.html.column
import kotlinx.html.FlowContent
import koala.css.*

fun FlowContent.eventsTab(
//    events: List<Event>
) {
    column(AlignItemsCenter) {
//        events.forEach { event ->
//            a {
//                href = "/event-portal/${event.eventId.value}"
//                card {
//                    label(event.title)
//                }
//            }
//        }
        homeFooter()
    }
}