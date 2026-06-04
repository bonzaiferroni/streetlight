package streetlight.web.layouts

import koala.Svg
import koala.SvgFile
import koala.html.AppRoute
import koala.model.Doc
import streetlight.model.data.BasicPost
import streetlight.model.data.Event
import streetlight.model.data.EventLocation
import streetlight.model.data.EventPost
import streetlight.model.data.Location
import streetlight.model.data.LocationPost
import streetlight.model.data.GalaxyPost
import streetlight.web.EventRoute
import streetlight.web.LocationRoute
import streetlight.web.PostRoute
import streetlight.web.SiteDocRoute

val Location.route get() = LocationRoute(slug)
val Event.route get() = EventRoute(slug)
val EventLocation.eventRoute get() = EventRoute(eventSlug)
val EventLocation.locationRoute get() = LocationRoute(locationSlug)
val Doc.route get() = SiteDocRoute(docId)
val BasicPost.postRoute get() = PostRoute(slug)

val GalaxyPost.route get(): AppRoute = when (this) {
    is BasicPost -> postRoute
    is EventPost -> event.eventRoute
    is LocationPost -> location.route
}

val GalaxyPost.subRoute get(): AppRoute? = when (this) {
    is BasicPost -> null
    is EventPost -> event.locationRoute
    is LocationPost -> null
}

val GalaxyPost.subtitle get(): String? = when (this) {
    is BasicPost -> sublabel
    is EventPost -> "${event.locationName}, ${event.city}"
    is LocationPost -> location.addressLine
}

val GalaxyPost.cells get() = when (this) {
    is BasicPost -> null
    is EventPost -> eventCells(event)
    is LocationPost -> locationCells(location)
}

val GalaxyPost.colorScheme get() = when (this) {
    is EventPost -> ColorScheme.Accent
    else -> ColorScheme.Primary
}

val GalaxyPost.flairIcon get() = when (this) {
    is EventPost -> FlairIcon.Event
    is LocationPost -> FlairIcon.Location
    else -> null
}

enum class ColorScheme(val cssValue: String) {
    Accent("var(--accent-fg)"),
    Primary("var(--primary-fg)"),
    Galaxy("var(--galaxy-fg)"),
}

enum class FlairIcon(val svg: Svg) {
    Event(SvgFile.Calendar),
    Location(SvgFile.Pin),
}