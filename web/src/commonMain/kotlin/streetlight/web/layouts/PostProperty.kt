package streetlight.web.layouts

import koala.Svg
import koala.SvgFile
import koala.html.AppRoute
import koala.model.Doc
import streetlight.model.data.City
import streetlight.model.data.Event
import streetlight.model.data.EventLocation
import streetlight.model.data.EventPost
import streetlight.model.data.Galaxy
import streetlight.model.data.Location
import streetlight.model.data.LocationPost
import streetlight.model.data.GalaxyPost
import streetlight.model.data.Media
import streetlight.model.data.MediaPost
import streetlight.model.data.Entity
import streetlight.web.CityRoute
import streetlight.web.EventRoute
import streetlight.web.GalaxyRoute
import streetlight.web.LocationRoute
import streetlight.web.MediaRoute
import streetlight.web.SiteDocRoute

val Location.route get() = LocationRoute(slug)
val Event.route get() = EventRoute(slug)
val EventLocation.eventRoute get() = EventRoute(eventSlug)
val EventLocation.locationRoute get() = LocationRoute(locationSlug)
val Doc.route get() = SiteDocRoute(docId)
val Media.route get() = MediaRoute(slug)
val Galaxy.route get() = GalaxyRoute(slug)

val GalaxyPost.route get(): AppRoute = when (this) {
    is MediaPost -> media.route
    is EventPost -> event.eventRoute
    is LocationPost -> location.route
}

val Entity.contentRoute get(): AppRoute = when (this) {
    is City -> CityRoute(slug)
    is EventLocation -> eventRoute
    is EventPost -> event.eventRoute
    is Galaxy -> route
    is LocationPost -> location.route
    is MediaPost -> media.route
    is Location -> route
    is Media -> route
}

val Entity.colorScheme get(): ColorScheme = when (this) {
    is City -> ColorScheme.City
    is EventLocation -> ColorScheme.Event
    is EventPost -> ColorScheme.Event
    is Galaxy -> ColorScheme.Galaxy
    is LocationPost -> ColorScheme.Location
    is Location -> ColorScheme.Location
    is MediaPost -> ColorScheme.Media
    is Media -> ColorScheme.Media
}

fun Entity.getCells(showMore: Boolean = false) = when(this) {
    is City -> null
    is EventLocation -> cellContentOf(this, showMore)
    is EventPost -> cellContentOf(event, showMore)
    is Galaxy -> cellContentOf(this)
    is LocationPost -> cellContentOf(location)
    is MediaPost -> null
    is Location -> cellContentOf(this)
    is Media -> null
}

val GalaxyPost.subRoute get(): AppRoute? = when (this) {
    is MediaPost -> null
    is EventPost -> event.locationRoute
    is LocationPost -> null
}

val GalaxyPost.subtitle get(): String? = when (this) {
    is MediaPost -> media.subtitle
    is EventPost -> "${event.locationName}, ${event.city}"
    is LocationPost -> location.addressLine
}

fun GalaxyPost.cellContent(showMore: Boolean) = when (this) {
    is MediaPost -> null
    is EventPost -> cellContentOf(event, showMore, this)
    is LocationPost -> cellContentOf(location)
}

val GalaxyPost.colorScheme get() = when (this) {
    is EventPost -> ColorScheme.Event
    is LocationPost -> ColorScheme.Location
    is MediaPost -> ColorScheme.Media
}

val GalaxyPost.flairIcon get() = when (this) {
    is EventPost -> FlairIcon.Event
    is LocationPost -> FlairIcon.Location
    else -> null
}

enum class FlairIcon(val svg: Svg) {
    Event(SvgFile.Calendar),
    Location(SvgFile.Pin),
}