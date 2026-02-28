package streetlight.web.shells

import koala.css.Width100
import koala.css.modify
import koala.html.AppRoute
import koala.html.SiteImage
import koala.html.ThumbImage
import koala.html.action
import koala.html.card
import koala.html.cardOf
import koala.html.image
import koala.html.row
import koala.html.textBlock
import kotlinx.html.FlowContent
import streetlight.model.data.Event
import streetlight.model.data.EventInfo
import streetlight.model.data.Location
import streetlight.web.EventIdRoute
import streetlight.web.LocationProfileRoute

fun FlowContent.cardOf(event: Event) {
    cardOf(EventIdRoute(event.eventId), event.title, event.thumbUrl, event.description)
}

fun FlowContent.cardOf(location: Location) {
    cardOf(LocationProfileRoute(location.locationId), location.name, location.thumbUrl, location.description)
}

fun FlowContent.cardOf(event: EventInfo) {
    cardOf(EventIdRoute(event.eventId), event.title, event.thumbUrl, event.description)
}