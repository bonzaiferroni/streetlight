package streetlight.web.shells

import koala.css.ModifierSet
import koala.html.SiteImage
import koala.html.cardOf
import kotlinx.html.FlowContent
import streetlight.model.data.Event
import streetlight.model.data.EventLocation
import streetlight.model.data.Galaxy
import streetlight.model.data.EventPost
import streetlight.model.data.Location
import streetlight.web.EventIdRoute
import streetlight.web.GalaxyPathIdRoute
import streetlight.web.LocationIdRoute

fun FlowContent.cardOf(event: Event, modifiers: ModifierSet? = null) {
    cardOf(EventIdRoute(event.eventId), event.title, event.thumbUrl, event.description, modifiers)
}

fun FlowContent.cardOf(location: Location) {
    cardOf(LocationIdRoute(location.locationId), location.name, location.thumbUrl, location.description)
}

fun FlowContent.cardOf(event: EventLocation) {
    cardOf(EventIdRoute(event.eventId), event.title, event.thumbUrl, event.description)
}

fun FlowContent.cardOf(galaxy: Galaxy) {
    cardOf(GalaxyPathIdRoute(galaxy.path), galaxy.name, SiteImage.placeholderThumb, galaxy.description)
}

fun FlowContent.cardOf(
    post: EventPost,
    modifiers: ModifierSet? = null,
) {
    val route = post.event?.eventId?.let { EventIdRoute(it) }
        ?: post.location?.locationId?.let { LocationIdRoute(it) }
    val thumbUrl = post.thumbUrl ?: SiteImage.placeholderThumb
    cardOf(route, post.title, thumbUrl, post.description, modifiers)
}