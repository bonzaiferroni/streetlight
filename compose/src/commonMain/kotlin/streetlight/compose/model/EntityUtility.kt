package streetlight.compose.model

import kabinet.utils.format
import kabinet.utils.toFutureFormat
import kabinet.utils.toMetricString
import kabinet.utils.toTimeFormat
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

/** A short fact shown with an entity, in [color]. */
data class EntityDetail(
    val color: ThemeColor,
    val text: String,
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

/** The details shown above this entity's title: when it happens, or what kind of thing it is. */
val Entity.topDetails: List<EntityDetail> get() = when (this) {
    is City -> listOf(EntityDetail(ThemeColor.City, state))
    is EventLocation -> startsAtDetails(startsAt)
    is EventPost -> event.topDetails
    is Event -> startsAtDetails(startsAt)
    is Galaxy -> listOfNotNull(tagline?.let { EntityDetail(ThemeColor.Galaxy, it) })
    is LocationPost -> location.topDetails
    is Location -> listOf(EntityDetail(ThemeColor.Location, mapType ?: "Location"))
    is MediaPost -> media.topDetails
    is Media -> listOfNotNull(subtitle?.let { EntityDetail(ThemeColor.Media, it) })
    is CustomEntity -> listOfNotNull(sublabel?.let { EntityDetail(toThemeColor(), it) })
    is Star -> listOfNotNull(tagline?.let { EntityDetail(ThemeColor.Primary, it) })
    is EventGroup -> startsAtDetails(startsAt)
    is Offering -> emptyList()
    is Skill -> listOf(EntityDetail(ThemeColor.Primary, skillLevel.label))
}

/** The details shown below this entity's title: where it is, and what it costs or holds. */
val Entity.bottomDetails: List<EntityDetail> get() = when (this) {
    is City -> countDetails(locationCount, eventCount)
    is EventLocation -> buildList {
        locationName?.let { add(EntityDetail(ThemeColor.Location, it)) }
        cost?.let { add(costDetail(it)) }
    }
    is EventPost -> event.bottomDetails
    is Event -> buildList {
        cost?.let { add(costDetail(it)) }
        tag?.let { add(EntityDetail(ThemeColor.Muted, it.label)) }
    }
    is Galaxy -> countDetails(locationCount, eventCount)
    is LocationPost -> location.bottomDetails
    is Location -> buildList {
        city?.let { add(EntityDetail(ThemeColor.Muted, it)) }
        if (eventCount > 0) add(EntityDetail(ThemeColor.Muted, "${eventCount.toMetricString()} events"))
    }
    is MediaPost -> media.bottomDetails
    is Media -> listOf(EntityDetail(ThemeColor.Muted, username.value))
    is CustomEntity -> emptyList()
    is Star -> listOf(EntityDetail(ThemeColor.Muted, username.value))
    is EventGroup -> buildList {
        locationName?.let { add(EntityDetail(ThemeColor.Location, it)) }
        add(EntityDetail(ThemeColor.Muted, "${eventCount.toMetricString()} events"))
    }
    is Offering -> listOf(EntityDetail(ThemeColor.Muted, priceLabel))
    is Skill -> listOf(EntityDetail(ThemeColor.Muted, "since $yearStarted"))
}

/** The day and time an event starts, as "Today" and "7:30 PM". */
private fun startsAtDetails(startsAt: Instant?) = when (startsAt) {
    null -> emptyList()
    else -> listOf(
        EntityDetail(ThemeColor.Event, startsAt.toFutureFormat()),
        EntityDetail(ThemeColor.Event, startsAt.toTimeFormat()),
    )
}

/** The cost of an event: "FREE", or the price. */
private fun costDetail(cost: Float) = EntityDetail(ThemeColor.Muted, if (cost == 0f) "FREE" else "$${cost.format(2, true)}")

/** The location and event counts of a place, each when it has any. */
private fun countDetails(locationCount: Int, eventCount: Int) = buildList {
    if (locationCount > 0) add(EntityDetail(ThemeColor.Muted, "${locationCount.toMetricString()} locations"))
    if (eventCount > 0) add(EntityDetail(ThemeColor.Muted, "${eventCount.toMetricString()} events"))
}
