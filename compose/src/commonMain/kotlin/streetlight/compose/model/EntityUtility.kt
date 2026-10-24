package streetlight.compose.model

import kabinet.utils.format
import kabinet.utils.toFutureFormat
import kabinet.utils.toMetricString
import kabinet.utils.toTimeFormat
import koala.SvgFile
import koala.Svg
import koala.SvgPack
import streetlight.model.data.City
import streetlight.model.data.CustomEntity
import streetlight.model.data.Entity
import streetlight.model.data.Event
import streetlight.model.data.EventGroup
import streetlight.model.data.EventLocation
import streetlight.model.data.EventPost
import streetlight.model.data.Galaxy
import streetlight.model.data.Location
import streetlight.model.data.LocationPost
import streetlight.model.data.Media
import streetlight.model.data.MediaPost
import streetlight.model.data.Offering
import streetlight.model.data.RecordType
import streetlight.model.data.Skill
import streetlight.model.data.Star
import streetlight.model.data.priceLabel
import kotlin.time.Instant

/**
 * A fact shown with an entity: its [text] after its [svg], in [color]. An entity type always gives the same details, so
 * a fact the entity lacks has `null` [text]. A fact of the entity itself takes the entity's color; a fact drawn from
 * another entity, such as an event's location, takes that entity's.
 */
data class EntityDetail(
    val color: ThemeColor,
    val svg: Svg,
    val text: String?,
)

/** The color of this entity's type. */
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

/** The details shown above this entity's title: when it happens, or what kind of thing it is. */
val Entity.topDetails: List<EntityDetail> get() = when (this) {
    is City -> listOf(EntityDetail(ThemeColor.City, SvgFile.Map.small, state))
    is EventLocation -> startsAtDetails(startsAt)
    is EventPost -> event.topDetails
    is Event -> startsAtDetails(startsAt)
    is Galaxy -> listOf(EntityDetail(ThemeColor.Galaxy, SvgFile.Planet.small, tagline))
    is LocationPost -> location.topDetails
    is Location -> listOf(EntityDetail(ThemeColor.Location, SvgFile.MapPin.small, mapType ?: "Location"))
    is MediaPost -> media.topDetails
    is Media -> listOf(EntityDetail(ThemeColor.Media, SvgFile.Quote.small, subtitle))
    is CustomEntity -> listOf(EntityDetail(toThemeColor(), SvgFile.Tag.small, sublabel))
    is Star -> listOf(EntityDetail(ThemeColor.Primary, SvgFile.Star.small, tagline))
    is EventGroup -> startsAtDetails(startsAt)
    is Offering -> emptyList()
    is Skill -> listOf(EntityDetail(ThemeColor.Primary, SvgFile.Award.small, skillLevel.label))
}

/** The details shown below this entity's title: where it is, and what it costs or holds. */
val Entity.bottomDetails: List<EntityDetail> get() = when (this) {
    is City -> countDetails(locationCount, eventCount)
    is EventLocation -> listOf(
        costDetail(cost),
        EntityDetail(ThemeColor.Location, SvgFile.MapPin.small, locationName),
    )
    is EventPost -> event.bottomDetails
    is Event -> listOf(
        costDetail(cost),
        EntityDetail(ThemeColor.Event, SvgFile.Label.small, tag?.label),
    )
    is Galaxy -> countDetails(locationCount, eventCount)
    is LocationPost -> location.bottomDetails
    is Location -> listOf(
        EntityDetail(ThemeColor.Muted, SvgFile.City.small, city),
        eventCountDetail(eventCount),
    )
    is MediaPost -> media.bottomDetails
    is Media -> listOf(EntityDetail(ThemeColor.Muted, SvgFile.User.small, username.value))
    is CustomEntity -> emptyList()
    is Star -> listOf(EntityDetail(ThemeColor.Muted, SvgFile.User.small, username.value))
    is EventGroup -> listOf(
        EntityDetail(ThemeColor.Location, SvgFile.MapPin.small, locationName),
        eventCountDetail(eventCount),
    )
    is Offering -> listOf(EntityDetail(ThemeColor.Muted, SvgFile.Coin.small, priceLabel))
    is Skill -> listOf(EntityDetail(ThemeColor.Muted, SvgFile.Calendar.small, "since $yearStarted"))
}

/** The day and time an event starts, as "Today" and "7:30 PM". */
private fun startsAtDetails(startsAt: Instant?) = listOf(
    EntityDetail(ThemeColor.Event, SvgFile.Calendar.small, startsAt?.toFutureFormat()),
    EntityDetail(ThemeColor.Event, SvgFile.Clock.small, startsAt?.toTimeFormat()),
)

/** The cost of an event: "FREE", the price, or `null` when it is not known. */
private fun costDetail(cost: Float?) = EntityDetail(
    color = ThemeColor.Event,
    svg = SvgFile.Ticket.small,
    text = cost?.let { if (it == 0f) "FREE" else "$${it.format(2, true)}" },
)

/** The location and event counts of a place. */
private fun countDetails(locationCount: Int, eventCount: Int) = listOf(
    EntityDetail(ThemeColor.Muted, SvgFile.MapPin.small, locationCount.takeIf { it > 0 }?.let { "${it.toMetricString()} locations" }),
    eventCountDetail(eventCount),
)

private fun eventCountDetail(eventCount: Int) =
    EntityDetail(ThemeColor.Muted, SvgFile.Calendar.small, eventCount.takeIf { it > 0 }?.let { "${it.toMetricString()} events" })
