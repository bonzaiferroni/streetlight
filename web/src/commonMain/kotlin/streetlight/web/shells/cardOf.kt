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
import streetlight.web.GalaxyRoute
import streetlight.web.LocationRoute
import streetlight.web.layouts.eventRoute
import streetlight.web.layouts.route

fun FlowContent.cardOf(event: Event, modifiers: ModifierSet? = null) {
    cardOf(event.route, event.title, event.images.thumb, event.description, modifiers)
}

fun FlowContent.cardOf(location: Location) {
    cardOf(LocationRoute(location.slug), location.label, location.images.thumb, location.description)
}

fun FlowContent.cardOf(event: EventLocation) {
    cardOf(event.eventRoute, event.title, event.images.thumb, event.description)
}

fun FlowContent.cardOf(galaxy: Galaxy) {
    cardOf(GalaxyRoute(galaxy.slug), galaxy.name, SiteImage.placeholderTh.url, galaxy.description)
}

fun FlowContent.cardOf(
    post: EventPost,
    modifiers: ModifierSet? = null,
) {
    val route = post.event?.eventRoute
    val thumbUrl = post.images.thumb ?: SiteImage.placeholderTh.url
    cardOf(route, post.title, thumbUrl, post.description, modifiers)
}