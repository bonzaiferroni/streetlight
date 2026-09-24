package streetlight.web.layouts

import koala.SvgFile
import koala.html.AppRoute
import kampfire.model.Url
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
import streetlight.model.data.RecordType
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
import kotlin.time.Instant

val Location.route get() = LocationRoute(slug)
val Event.route get() = EventRoute(slug)
val EventLocation.eventRoute get() = EventRoute(eventSlug)
val EventLocation.locationRoute get() = LocationRoute(locationSlug)
val Doc.route get() = SiteDocRoute(docId)
val Media.route get() = MediaRoute(slug)
val Galaxy.route get() = GalaxyRoute(slug)

/** The page of this entity, the event's for an event at a location. */
fun Entity.toRoute(): AppRoute? = when (this) {
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

fun Entity.toThemeColor(): ThemeColor = when (this) {
    is City -> ThemeColor.City
    is EventLocation -> ThemeColor.Event
    is EventPost -> ThemeColor.Event
    is Event -> ThemeColor.Event
    is Galaxy -> ThemeColor.Galaxy
    is LocationPost -> ThemeColor.Location
    is Location -> ThemeColor.Location
    is MediaPost -> ThemeColor.Media
    is Media -> ThemeColor.Media
    is CustomEntity -> when (recordType) {
        RecordType.Location -> ThemeColor.Location
        RecordType.Event -> ThemeColor.Event
        else -> ThemeColor.Primary
    }
    is Star -> ThemeColor.Primary
}

fun Entity.toFlair(): FlairIcon = when (this) {
    is EventLocation,is EventPost, is Event -> FlairIcon.Event
    is LocationPost, is Location -> FlairIcon.Location
    is MediaPost, is Media -> FlairIcon.Media
    is CustomEntity -> when (recordType) {
        RecordType.Location -> FlairIcon.Location
        RecordType.Event -> FlairIcon.Event
        else -> FlairIcon.Default
    }
    else -> FlairIcon.Default
}

/** The facts shown in this entity's [cellGrid], or `null` when it has none. */
fun Entity.toCells(): List<EntityCell>? = when (this) {
    is City -> buildList {
        if (locationCount > 0) add(locationCountCell(locationCount))
        if (eventCount > 0) add(eventCountCell(eventCount))
    }
    is EventLocation -> eventCells(startsAt, cost, url, locationName)
    is EventPost -> event.toCells()
    is Event -> eventCells(startsAt, cost, website, null)
    is Galaxy -> buildList {
        if (locationCount > 0) add(locationCountCell(locationCount))
        if (eventCount > 0) add(eventCountCell(eventCount))
    }
    is LocationPost -> location.toCells()
    is MediaPost -> null
    is Location -> locationCells(mapType, city, eventCount)
    is Media -> null
    is CustomEntity -> null
    is Star -> null
}

/** The cells of an event: its date, time, cost linking to [purchaseUrl], and [locationName]. */
fun eventCells(startsAt: Instant?, cost: Float?, purchaseUrl: Url?, locationName: String?) = buildList {
    startsAt?.let { add(dateCell(it)); add(startsAtCell(it)) }
    cost?.let { add(costCell(it, purchaseUrl)) }
    locationName?.let { add(EntityCell(SvgFile.MapPin, it, null)) }
}

/** The cells of a location: its [mapType], [city], and [eventCount] when it has events. */
fun locationCells(mapType: String?, city: String?, eventCount: Int) = buildList {
    add(EntityCell(SvgFile.MapPin, mapType ?: "Location", null))
    city?.let { add(EntityCell(SvgFile.City, it, null)) }
    if (eventCount > 0) add(eventCountCell(eventCount))
}

/**
 * The buttons of [entity]'s [cellGrid]: its star toggle, the more button when [showMore], and the post menu of a
 * post.
 */
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

/** A second route shown under the title, such as the location of an event post. */
fun Entity.toSubRoute(): AppRoute? = when (this) {
    is MediaPost -> null
    is EventPost -> event.locationRoute
    is LocationPost -> null
    else -> null
}

fun Entity.toSubtitle(): String? = when (this) {
    is MediaPost -> media.subtitle
    is EventPost -> "${event.locationName}, ${event.city}"
    is LocationPost -> location.addressLine
    else -> null
}
