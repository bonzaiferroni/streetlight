package streetlight.web.shells

import koala.css.AlignItemsStart
import koala.css.FlexItems1
import koala.css.QueryRow
import koala.css.modify
import koala.html.button
import koala.html.column
import koala.html.heading3
import kotlinx.html.FlowContent
import streetlight.model.data.Event
import streetlight.model.data.Location
import streetlight.web.ChatRoute
import streetlight.web.EventScoutRoute
import streetlight.web.CreateLocationRoute
import streetlight.web.EditPostRoute
import streetlight.web.SandboxRoute
import streetlight.web.pages.appFooter

fun FlowContent.spotlightTab(events: List<Event>, locations: List<Location>) {
    column {
        column(modify(QueryRow, FlexItems1, AlignItemsStart)) {
            column {
                heading3("Events")
                events.take(10).forEach { event ->
                    cardOf(event)
                }
            }
            column {
                heading3("Locations")
                locations.take(10).forEach { location ->
                    cardOf(location)
                }
            }
        }

        button("Create Event", EventScoutRoute())
        button("Create location", CreateLocationRoute)
        button("Create story", EditPostRoute())
        button("Chat", ChatRoute)
        button("Go to sandbox", SandboxRoute)
        appFooter()
    }
}