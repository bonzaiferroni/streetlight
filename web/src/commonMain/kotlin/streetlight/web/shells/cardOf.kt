package streetlight.web.shells

import kampfire.model.thumb
import koala.SiteImage
import koala.css.ModifierSet
import koala.html.cardOf
import kotlinx.html.FlowContent
import streetlight.model.data.Event
import streetlight.model.data.EventLocation
import streetlight.model.data.Galaxy
import streetlight.model.data.EventPost
import streetlight.model.data.Location
import streetlight.web.GalaxySlugRoute
import streetlight.web.LocationIdRoute
import streetlight.web.layouts.route

fun FlowContent.cardOf(event: Event, modifiers: ModifierSet? = null) {
    cardOf(event.route, event.title, event.images.thumb, event.description, modifiers)
}

fun FlowContent.cardOf(location: Location) {
    cardOf(LocationIdRoute(location.locationId), location.name, location.images.thumb, location.description)
}

fun FlowContent.cardOf(event: EventLocation) {
    cardOf(event.route, event.title, event.images.thumb, event.description)
}

fun FlowContent.cardOf(galaxy: Galaxy) {
    cardOf(GalaxySlugRoute(galaxy.slug), galaxy.name, SiteImage.placeholderTh.url, galaxy.description)
}

fun FlowContent.cardOf(
    post: EventPost,
    modifiers: ModifierSet? = null,
) {
    val route = post.event?.route ?: post.location?.locationId?.let { LocationIdRoute(it) }
    val thumbUrl = post.images.thumb ?: SiteImage.placeholderTh.url
    cardOf(route, post.title, thumbUrl, post.description, modifiers)
}