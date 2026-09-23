package streetlight.web.layouts

import kabinet.utils.toMetricString
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
import streetlight.model.data.Entity
import streetlight.model.data.Star
import streetlight.model.ui.CityRoute
import streetlight.model.ui.EventRoute
import streetlight.model.ui.GalaxyRoute
import streetlight.model.ui.LocationRoute
import streetlight.model.ui.MediaRoute
import streetlight.model.ui.SiteDocRoute
import streetlight.model.ui.StarRoute
import streetlight.web.ui.postMenu
import streetlight.web.ui.starToggle

val Location.route get() = LocationRoute(slug)
val Event.route get() = EventRoute(slug)
val EventLocation.eventRoute get() = EventRoute(eventSlug)
val EventLocation.locationRoute get() = LocationRoute(locationSlug)
val Doc.route get() = SiteDocRoute(docId)
val Media.route get() = MediaRoute(slug)
val Galaxy.route get() = GalaxyRoute(slug)

val Entity.contentRoute get(): AppRoute? = when (this) {
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
    is Star -> StarRoute(username)
}

val Entity.themeColor get(): ThemeColor = when (this) {
    is City -> ThemeColor.City
    is EventLocation -> ThemeColor.Event
    is EventPost -> ThemeColor.Event
    is Event -> ThemeColor.Event
    is Galaxy -> ThemeColor.Galaxy
    is LocationPost -> ThemeColor.Location
    is Location -> ThemeColor.Location
    is MediaPost -> ThemeColor.Media
    is Media -> ThemeColor.Media
    is CustomEntity -> ThemeColor.Primary
    is Star -> ThemeColor.Primary
}

val Entity.flair get(): FlairIcon = when (this) {
    is EventLocation,is EventPost, is Event -> FlairIcon.Event
    is LocationPost, is Location -> FlairIcon.Location
    is MediaPost, is Media -> FlairIcon.Media
    else -> FlairIcon.Default
}

val Entity.cells get(): List<EntityCell>? = when (this) {
    is City -> null
    is EventLocation -> listOfNotNull(
        startsAt?.let { dateCell(it) },
        startsAtCell(startsAt),
        cost?.let { costCell(it, url) },
        locationName?.let { EntityCell(SvgFile.MapPin, it, null) },
    )
    is EventPost -> event.cells
    is Event -> listOfNotNull(
        startsAt?.let { dateCell(it) },
        startsAtCell(startsAt),
        cost?.let { costCell(it, website) },
    )
    is Galaxy -> listOf(
        EntityCell(SvgFile.Calendar, eventCount.toMetricString(), null),
    )
    is LocationPost -> location.cells
    is MediaPost -> null
    is Location -> listOfNotNull(
        EntityCell(SvgFile.MapPin, mapType ?: "Location", null),
        city?.let { EntityCell(SvgFile.City, it, null) },
    )
    is Media -> null
    is CustomEntity -> null
    is Star -> null
}

fun entityButtonsOf(entity: Entity, showMore: Boolean): List<EntityButton>? = when (entity) {
    is City -> null
    is EventLocation -> buildList {
        add(EntityButton { starToggle(entity) })
        if (showMore) add(EntityButton { moreButton() })
    }
    is EventPost -> entityButtonsOf(entity.event, showMore).orEmpty() + EntityButton {
        postMenu(entity.post.postId, entity.post.username)
    }
    is Event -> buildList {
        add(EntityButton { starToggle(entity) })
        if (showMore) add(EntityButton { moreButton() })
    }
    is Galaxy -> listOf(
        EntityButton { starToggle(entity) },
    )
    is LocationPost -> entityButtonsOf(entity.location, showMore).orEmpty() + EntityButton {
        postMenu(entity.post.postId, entity.post.username)
    }
    is MediaPost -> null
    is Location -> buildList {
        add(EntityButton { starToggle(entity) })
        if (showMore) add(EntityButton { moreButton() })
    }
    is Media -> null
    is CustomEntity -> null
    is Star -> null
}

val Entity.subRoute get(): AppRoute? = when (this) {
    is MediaPost -> null
    is EventPost -> event.locationRoute
    is LocationPost -> null
    else -> null
}

val Entity.subtitle get(): String? = when (this) {
    is MediaPost -> media.subtitle
    is EventPost -> "${event.locationName}, ${event.city}"
    is LocationPost -> location.addressLine
    else -> null
}
