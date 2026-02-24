package streetlight.web.shells

import koala.css.AlignItemsStart
import koala.css.FlexItems1
import koala.css.Width100
import koala.css.modify
import koala.html.action
import koala.html.button
import koala.html.column
import koala.html.heading3
import koala.html.row
import koala.html.textBlock
import kotlinx.html.FlowContent
import streetlight.model.data.Event
import streetlight.model.data.Location
import streetlight.web.ChatRoute
import streetlight.web.CreateEventRoute
import streetlight.web.EditPostRoute
import streetlight.web.EventIdRoute
import streetlight.web.EventObjectRoute
import streetlight.web.LocationProfileRoute
import streetlight.web.SandboxRoute
import streetlight.web.pages.appFooter

fun FlowContent.hapsTab(events: List<Event>, locations: List<Location>) {
    column {
        row(modify(FlexItems1, AlignItemsStart)) {
            column {
                heading3("Events")
                events.forEach { event ->
                    action(EventIdRoute(event.eventId)) {
                        textBlock(event.title)
                    }
                }
            }
            column {
                heading3("Locations")
                locations.forEach { location ->
                    action(LocationProfileRoute(location.locationId)) {
                        textBlock(location.name)
                    }
                }
            }
        }

        button("Create Event", CreateEventRoute)
        button("Create story", EditPostRoute())
        button("Chat", ChatRoute)
        button("Go to sandbox", SandboxRoute)
        appFooter()
    }
}