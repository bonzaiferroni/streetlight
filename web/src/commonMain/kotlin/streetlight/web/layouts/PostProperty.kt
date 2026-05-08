package streetlight.web.layouts

import koala.SvgFile
import koala.html.AppRoute
import koala.model.Doc
import streetlight.model.data.StarPost
import streetlight.model.data.Event
import streetlight.model.data.EventLocation
import streetlight.model.data.EventPost
import streetlight.model.data.Location
import streetlight.model.data.LocationPost
import streetlight.model.data.Post
import streetlight.model.data.addressLineOf
import streetlight.web.EventSlugRoute
import streetlight.web.LocationIdRoute
import streetlight.web.StarPostRoute
import streetlight.web.SiteDocRoute

val Location.route get() = LocationIdRoute(locationId)
val Event.route get() = EventSlugRoute(slug)
val EventLocation.eventRoute get() = EventSlugRoute(slug)
val EventLocation.locationRoute get() = LocationIdRoute(locationId)
val Doc.route get() = SiteDocRoute(docId)
val StarPost.starPostRoute get() = StarPostRoute(slug ?: postId.value)

val Post.route get(): AppRoute = when (this) {
    is StarPost -> starPostRoute
    is EventPost -> event.eventRoute
    is LocationPost -> location.route
}

val Post.subRoute get(): AppRoute? = when (this) {
    is StarPost -> null
    is EventPost -> event.locationRoute
    is LocationPost -> null
}

val Post.subtitle get(): String? = when (this) {
    is StarPost -> subtitle
    is EventPost -> "${event.locationName}, ${event.city}"
    is LocationPost -> location.addressLine
}

val Post.cells get() = when (this) {
    is StarPost -> null
    is EventPost -> eventCells(event)
    is LocationPost -> locationCells(location)
}

val Post.colorScheme get() = when (this) {
    is EventPost -> "var(--accent-fg)"
    else -> "var(--primary-fg)"
}

val Post.flairIcon get() = when (this) {
    is EventPost -> SvgFile.Calendar
    is LocationPost -> SvgFile.Pin
    else -> null
}