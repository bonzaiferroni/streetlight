package streetlight.web.layouts

import koala.Svg
import koala.SvgFile
import koala.html.AppRoute
import koala.model.Doc
import streetlight.model.data.City
import streetlight.model.data.CustomEntity
import streetlight.model.data.Event
import streetlight.model.data.EventLocation
import streetlight.model.data.EventPost
import streetlight.model.data.Galaxy
import streetlight.model.data.Location
import streetlight.model.data.LocationPost
import streetlight.model.data.Media
import streetlight.model.data.MediaPost
import streetlight.model.data.FeedEntity
import streetlight.model.ui.CityRoute
import streetlight.model.ui.EventRoute
import streetlight.model.ui.GalaxyRoute
import streetlight.model.ui.LocationRoute
import streetlight.model.ui.MediaRoute
import streetlight.model.ui.SiteDocRoute

val Location.route get() = LocationRoute(slug)
val Event.route get() = EventRoute(slug)
val EventLocation.eventRoute get() = EventRoute(eventSlug)
val EventLocation.locationRoute get() = LocationRoute(locationSlug)
val Doc.route get() = SiteDocRoute(docId)
val Media.route get() = MediaRoute(slug)
val Galaxy.route get() = GalaxyRoute(slug)

val FeedEntity.contentRoute get(): AppRoute? = when (this) {
    is City -> CityRoute(slug)
    is EventLocation -> eventRoute
    is EventPost -> event.eventRoute
    is Event -> route
    is Galaxy -> route
    is LocationPost -> location.route
    is MediaPost -> media.route
    is Location -> route
    is Media -> route
    is CustomEntity -> route
}

val FeedEntity.colorScheme get(): ColorScheme = when (this) {
    is City -> ColorScheme.City
    is EventLocation -> ColorScheme.Event
    is EventPost -> ColorScheme.Event
    is Event -> ColorScheme.Event
    is Galaxy -> ColorScheme.Galaxy
    is LocationPost -> ColorScheme.Location
    is Location -> ColorScheme.Location
    is MediaPost -> ColorScheme.Media
    is Media -> ColorScheme.Media
    is CustomEntity -> ColorScheme.Primary
}

val FeedEntity.flair get(): FlairIcon = when (this) {
    is EventLocation,is EventPost, is Event -> FlairIcon.Event
    is LocationPost, is Location -> FlairIcon.Location
    is MediaPost, is Media -> FlairIcon.Media
    else -> FlairIcon.Default
}

fun FeedEntity.getCells(showMore: Boolean = false) = when(this) {
    is City -> null
    is EventLocation -> cellContentOf(this, showMore)
    is EventPost -> cellContentOf(event, showMore)
    is Event -> cellContentOf(this)
    is Galaxy -> cellContentOf(this)
    is LocationPost -> cellContentOf(location)
    is MediaPost -> null
    is Location -> cellContentOf(this)
    is Media -> null
    is CustomEntity -> null
}

val FeedEntity.subRoute get(): AppRoute? = when (this) {
    is MediaPost -> null
    is EventPost -> event.locationRoute
    is LocationPost -> null
    else -> null
}

val FeedEntity.subtitle get(): String? = when (this) {
    is MediaPost -> media.subtitle
    is EventPost -> "${event.locationName}, ${event.city}"
    is LocationPost -> location.addressLine
    else -> null
}

fun FeedEntity.cellContent(showMore: Boolean) = when (this) {
    is MediaPost -> null
    is EventPost -> cellContentOf(event, showMore, post)
    is LocationPost -> cellContentOf(location, post)
    else -> null
}