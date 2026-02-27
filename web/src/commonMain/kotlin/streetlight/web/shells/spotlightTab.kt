package streetlight.web.shells

import koala.css.AlignItemsStart
import koala.css.FlexItems1
import koala.css.Width100
import koala.css.modify
import koala.html.AppRoute
import koala.html.SiteImage
import koala.html.ThumbImage
import koala.html.action
import koala.html.button
import koala.html.card
import koala.html.column
import koala.html.heading3
import koala.html.image
import koala.html.row
import koala.html.textBlock
import kotlinx.html.FlowContent
import streetlight.model.data.Event
import streetlight.model.data.Location
import streetlight.web.ChatRoute
import streetlight.web.ReadEventRoute
import streetlight.web.CreateLocationRoute
import streetlight.web.EditPostRoute
import streetlight.web.EventIdRoute
import streetlight.web.LocationProfileRoute
import streetlight.web.SandboxRoute
import streetlight.web.pages.appFooter

fun FlowContent.spotlightTab(events: List<Event>, locations: List<Location>) {
    column {
        row(modify(FlexItems1, AlignItemsStart)) {
            column {
                heading3("Events")
                events.take(10).forEach { event ->
                    spotlightCard(EventIdRoute(event.eventId), event.title, event.thumbUrl)
                }
            }
            column {
                heading3("Locations")
                locations.take(10).forEach { location ->
                    spotlightCard(LocationProfileRoute(location.locationId), location.name, location.thumbUrl)
                }
            }
        }

        button("Create Event", ReadEventRoute())
        button("Create location", CreateLocationRoute)
        button("Create story", EditPostRoute())
        button("Chat", ChatRoute)
        button("Go to sandbox", SandboxRoute)
        appFooter()
    }
}

fun FlowContent.spotlightCard(
    route: AppRoute,
    title: String,
    thumbUrl: String?,
) {
    action(route, modify(Width100)) {
        card {
            row {
                image(thumbUrl, modify(ThumbImage), SiteImage.placeholderThumb)
                textBlock(title)
            }
        }
    }
}