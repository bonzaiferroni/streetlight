package streetlight.web.model

import koala.SvgPack
import koala.SvgFile
import koala.model.WebRoute
import kampfire.model.Url
import koala.model.Doc
import streetlight.model.data.EventGroup
import streetlight.model.data.City
import streetlight.model.data.CustomEntity
import streetlight.model.data.Event
import streetlight.model.data.EventTag
import streetlight.model.data.FeedContext
import streetlight.model.data.EventLocation
import streetlight.model.data.EventPost
import streetlight.model.data.Galaxy
import streetlight.model.data.Location
import streetlight.model.data.LocationPost
import streetlight.model.data.Media
import streetlight.model.data.Offering
import streetlight.model.data.Skill
import streetlight.model.data.priceLabel
import streetlight.model.data.MediaPost
import streetlight.model.data.Entity
import streetlight.model.data.EntityRef
import streetlight.model.data.EntityType
import streetlight.model.data.RecordType
import streetlight.model.data.Star
import streetlight.model.ui.CityRoute
import streetlight.model.ui.EventRoute
import streetlight.model.ui.GalaxyRoute
import streetlight.model.ui.LocationRoute
import streetlight.model.ui.MediaRoute
import streetlight.model.ui.OfferingRoute
import streetlight.model.ui.SkillRoute
import streetlight.model.ui.SiteDocRoute
import streetlight.model.ui.StarRoute
import streetlight.web.component.postMenu
import streetlight.web.component.starToggle
import kotlin.time.Instant
import streetlight.web.layout.EntityButton
import streetlight.web.layout.EntityCell
import streetlight.web.layout.EntityDialog
import streetlight.web.layout.cellGrid
import streetlight.web.layout.costCell
import streetlight.web.layout.dateCell
import streetlight.web.layout.eventCountCell
import streetlight.web.layout.locationCountCell
import streetlight.web.layout.startsAtCell

val Location.route get() = LocationRoute(slug)
val Event.route get() = EventRoute(slug)
val EventLocation.eventRoute get() = EventRoute(eventSlug)
val EventLocation.locationRoute get() = LocationRoute(locationSlug)
val Doc.route get() = SiteDocRoute(docId)
val Media.route get() = MediaRoute(slug)
val Galaxy.route get() = GalaxyRoute(slug)

/** The page of this entity, the event's for an event at a location. */
fun Entity.toRoute(): WebRoute? = when (this) {
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
    is EventGroup -> null
    is Offering -> OfferingRoute(offeringId)
    is Skill -> SkillRoute(skillId)
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
    is EventGroup -> ThemeColor.Location
    is Offering -> ThemeColor.Primary
    is Skill -> ThemeColor.Primary
}

/** The flair badge icon of this entity, the icon of its tag for an event that has one. */
fun Entity.toFlair(): SvgPack = when (this) {
    is EventLocation -> tag?.svg ?: FlairIcon.Event.svg
    is EventPost -> tag?.svg ?: FlairIcon.Event.svg
    is Event -> tag?.svg ?: FlairIcon.Event.svg
    is LocationPost, is Location -> FlairIcon.Location.svg
    is MediaPost, is Media -> FlairIcon.Media.svg
    is Skill -> tags?.firstOrNull()?.svg ?: SvgFile.Award
    is CustomEntity -> when (recordType) {
        RecordType.Location -> FlairIcon.Location.svg
        RecordType.Event -> FlairIcon.Event.svg
        else -> FlairIcon.Default.svg
    }
    else -> FlairIcon.Default.svg
}

/** The record whose whole entity the [EntityDialog] reads, or `null` for an entity it does not show. */
fun Entity.toEntityRef(): EntityRef? = when (this) {
    is EventLocation -> EntityRef(EntityType.Event, eventSlug)
    is EventPost -> event.toEntityRef()
    is Event -> EntityRef(EntityType.Event, slug)
    is LocationPost -> location.toEntityRef()
    is Location -> EntityRef(EntityType.Location, slug)
    is MediaPost -> media.toEntityRef()
    is Media -> EntityRef(EntityType.Media, slug)
    is City -> null
    is Galaxy -> null
    is CustomEntity -> null
    is Star -> null
    is EventGroup -> null
    is Offering -> null
    is Skill -> null
}

/**
 * The facts shown in this entity's [cellGrid], or `null` when it has none. Shown in the feed of a location's own page,
 * as [context] says, an event leaves out its location.
 */
fun Entity.toCells(context: FeedContext? = null): List<EntityCell>? = when (this) {
    is City -> buildList {
        if (locationCount > 0) add(locationCountCell(locationCount))
        if (eventCount > 0) add(eventCountCell(eventCount))
    }
    is EventLocation -> when (context) {
        is FeedContext.Location -> eventCells(startsAt, cost, url, tag, null)
        else -> eventCells(startsAt, cost, url, tag, locationLabel, locationRoute)
    }
    is EventPost -> event.toCells(context)
    is Event -> eventCells(startsAt, cost, url, tag, null)
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
    is EventGroup -> null
    is Offering -> listOf(EntityCell(SvgFile.Coin.small, priceLabel, null))
    is Skill -> listOf(
        EntityCell(SvgFile.Award.small, skillLevel.label, null),
        EntityCell(SvgFile.Calendar.small, "since $yearStarted", null),
    )
}

/**
 * The cells of an event: its date, time, cost linking to [purchaseUrl], its [tag], and [locationName]
 * linking to [locationRoute].
 */
fun eventCells(
    startsAt: Instant?,
    cost: Float?,
    purchaseUrl: Url?,
    tag: EventTag?,
    locationName: String?,
    locationRoute: WebRoute? = null,
) = buildList {
    startsAt?.let { add(dateCell(it)); add(startsAtCell(it)) }
    cost?.let { add(costCell(it, purchaseUrl)) }
    tag?.let { add(EntityCell(SvgFile.Label.small, it.label, null)) }
    locationName?.let {
        val locationUrl = locationRoute?.let { route -> Url(route.toRelativePath()) }
        add(EntityCell(SvgFile.MapPin.small, it, locationUrl, themeColor = ThemeColor.Location))
    }
}

/** The cells of a location: its [mapType], [city], and [eventCount] when it has events. */
fun locationCells(mapType: String?, city: String?, eventCount: Int) = buildList {
    add(EntityCell(SvgFile.MapPin.small, mapType ?: "Location", null))
    city?.let { add(EntityCell(SvgFile.City.small, it, null)) }
    if (eventCount > 0) add(eventCountCell(eventCount))
}

/** The buttons of this entity's [cellGrid]: its star toggle, and the post menu of a post. */
fun Entity.toButtons(): List<EntityButton>? = when (val entity = this) {
    is City -> null
    is EventLocation -> buildList {
        add(EntityButton { starToggle(entity) })
    }
    is EventPost -> entity.event.toButtons().orEmpty() + EntityButton {
        postMenu(entity.post.postId, entity.post.username)
    }
    is Event -> buildList {
        add(EntityButton { starToggle(entity) })
    }
    is Galaxy -> listOf(
        EntityButton { starToggle(entity) },
    )
    is LocationPost -> entity.location.toButtons().orEmpty() + EntityButton {
        postMenu(entity.post.postId, entity.post.username)
    }
    is MediaPost -> null
    is Location -> buildList {
        add(EntityButton { starToggle(entity) })
    }
    is Media -> null
    is CustomEntity -> null
    is Star -> null
    is EventGroup -> null
    is Offering -> null
    is Skill -> null
}

/** A second route shown under the title, such as the location of an event post. */
fun Entity.toSubRoute(): WebRoute? = when (this) {
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
