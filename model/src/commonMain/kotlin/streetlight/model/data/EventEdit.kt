package streetlight.model.data

import kampfire.model.Url
import kampfire.model.toValidityCheck
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable
import kotlin.getValue

@Serializable
data class EventEdit(
    val eventId: EventId? = null,
    val title: String? = null,
    val locationId: LocationId? = null,
    val description: String? = null,
    val contact: String? = null,
    val invitation: String? = null,
    val ageMin: Int? = null,
    val cost: Float? = null,
    val links: List<ExtraLink>? = null,
    val isHost: Boolean? = null,
    val url: String? = null,
    val sourceUrl: String? = null,
    val sourceImageUrl: String? = null,
    val imageRef: Url? = null,
    val startTime: LocalTime? = null,
    val endTime: LocalTime? = null,
    val date: LocalDate? = null,
    val timeZoneId: String? = null,
) {
    val isFree get() = cost == 0f
    val timeZone
        get() = try {
            timeZoneId?.let { TimeZone.of(it) }
        } catch (_: Exception) { // fails in browser
            TimeZone.currentSystemDefault()
        }

    val startsAt get() = startTime?.let { timeZone?.let { date?.atTime(startTime)?.toInstant(it) } }
    val endsAt get() = endTime?.let { timeZone?.let { date?.atTime(endTime)?.toInstant(it) } }

    val displayedLinks by lazy {
        buildList {
            url?.let { url ->
                add(ExtraLink("web page", url))
                cost?.takeIf { it > 0 }?.let {
                    add(ExtraLink("tickets", url))
                }
            }
            links?.let {
                addAll(it)
            }
        }.takeIf { it.isNotEmpty() }
    }

    val validity by lazy {
        buildSet {
            if (title.isNullOrBlank()) add(EventProperty.Title)
            // if (locationId == null) add(EventProperty.Location)
            if (startTime == null) add(EventProperty.StartTime)
            if (date == null) add(EventProperty.Date)
            // if (timeZoneId == null) add(EventProperty.TimeZone)
            if (cost == null) add(EventProperty.Cost)
        }.toValidityCheck()
    }
}

object EventProperty {
    val Title = "title"
    val Location = "location"
    val StartTime = "start time"
    val Date = "date"
    val TimeZone = "time zone"
    val Cost = "cost"
}

fun Event.toEdit() = EventEdit(
    eventId = eventId,
    title = title,
    locationId = locationId,
    imageRef = imageRef,
    description = description,
    contact = contact,
    invitation = invitation,
    ageMin = ageMin,
    cost = cost,
    links = links,
    url = url,
    sourceUrl = sourceUrl,
    sourceImageUrl = sourceImageUrl,
    startTime = startsAt.toLocalDateTime(timeZone).time,
    date = startsAt.toLocalDateTime(timeZone).date,
    timeZoneId = timeZone.id,
)

fun EventEdit.mergeLeft(other: EventEdit?) = other?.let {
    EventEdit(
        eventId = eventId ?: it.eventId,
        title = title ?: it.title,
        locationId = locationId ?: it.locationId,
        imageRef = imageRef ?: it.imageRef,
        description = description ?: it.description,
        contact = contact ?: it.contact,
        invitation = invitation ?: it.invitation,
        ageMin = ageMin ?: it.ageMin,
        cost = cost ?: it.cost,
        isHost = isHost ?: it.isHost,
        links = links ?: it.links,
        url = url ?: it.url,
        sourceUrl = sourceUrl ?: it.sourceUrl,
        sourceImageUrl = sourceImageUrl ?: it.sourceImageUrl,
        date = date ?: it.date,
        startTime = startTime ?: it.startTime,
        endTime = endTime ?: it.endTime,
        timeZoneId = timeZoneId ?: it.timeZoneId,
    )
} ?: this

fun EventEdit.mergeRight(other: EventEdit?) = other?.mergeLeft(this) ?: this