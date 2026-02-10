package streetlight.web

import koala.css.*
import koala.dom.*
import streetlight.web.pages.appFooter

fun RenderContext.eventsTab(
    portal: AppPortal
) {
    column(modify(AlignItemsCenter)) {
//        events.forEach { event ->
//            a {
//                href = "/event-portal/${event.eventId.value}"
//                card {
//                    label(event.title)
//                }
//            }
//        }
        button("Go to sandbox", onClick ={
            portal.go(SandboxRoute)
        })
        appFooter()
    }
}