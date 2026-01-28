package streetlight.web.pages

import koala.html.column
import kotlinx.html.FlowContent
import koala.css.*
import kotlinx.html.TagConsumer

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