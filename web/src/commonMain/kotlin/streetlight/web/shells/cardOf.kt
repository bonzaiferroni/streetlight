package streetlight.web.shells

import kampfire.utils.takeEllipsis
import koala.css.ModifierSet
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
import streetlight.model.data.Galaxy
import streetlight.model.data.GalaxyPost
import streetlight.model.data.Location
import streetlight.web.EventIdRoute
import streetlight.web.GalaxyPathIdRoute
import streetlight.web.LocationProfileRoute

fun FlowContent.cardOf(event: Event, modifiers: ModifierSet? = null) {
    cardOf(EventIdRoute(event.eventId), event.title, event.thumbUrl, event.description, modifiers)
}

fun FlowContent.cardOf(location: Location) {
    cardOf(LocationProfileRoute(location.locationId), location.name, location.thumbUrl, location.description)
}

fun FlowContent.cardOf(event: EventInfo) {
    cardOf(EventIdRoute(event.eventId), event.title, event.thumbUrl, event.description)
}

fun FlowContent.cardOf(galaxy: Galaxy) {
    cardOf(GalaxyPathIdRoute(galaxy.pathId), galaxy.name, SiteImage.placeholderThumb, galaxy.description)
}

fun FlowContent.cardOf(post: GalaxyPost) {
    val route = post.event?.eventId?.let { EventIdRoute(it) }
        ?: post.location?.locationId?.let { LocationProfileRoute(it) }
        ?: return
    val title = post.title ?: return
    val thumbUrl = post.thumbUrl ?: SiteImage.placeholderThumb
    cardOf(route, title, thumbUrl, post.description)
}