package streetlight.web.shells

import koala.SiteImage
import koala.css.ModifierSet
import koala.html.cardOf
import kotlinx.html.FlowContent
import streetlight.model.data.Event
import streetlight.model.data.EventLocation
import streetlight.model.data.Galaxy
import streetlight.model.data.EventPost
import streetlight.model.data.Location
import streetlight.model.ui.GalaxyRoute
import streetlight.model.ui.LocationRoute
import streetlight.web.layouts.eventRoute
import streetlight.web.layouts.route

fun FlowContent.cardOf(event: Event, mod: ModifierSet? = null) {
    cardOf(event.route, event.title, event.image?.thumb, event.description?.value, mod)
}

fun FlowContent.cardOf(location: Location) {
    cardOf(LocationRoute(location.slug), location.label, location.image?.thumb, location.description?.value)
}

fun FlowContent.cardOf(event: EventLocation) {
    cardOf(event.eventRoute, event.title, event.image.thumb, event.description?.value)
}

fun FlowContent.cardOf(galaxy: Galaxy) {
    cardOf(GalaxyRoute(galaxy.slug), galaxy.name, SiteImage.placeholderTh, galaxy.description?.value)
}

fun FlowContent.cardOf(
    post: EventPost,
    mod: ModifierSet? = null,
) {
    val route = post.event.eventRoute
    val thumbUrl = post.image?.thumb ?: SiteImage.placeholderTh
    cardOf(route, post.label, thumbUrl, post.body?.value, mod)
}