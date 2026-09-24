package streetlight.model.data

import kampfire.api.Markdown
import kampfire.model.Url
import kampfire.model.toValidityCheck
import koala.Image
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable
import kotlin.getValue

/** The fields of an event a form sends, with the date and times it is scheduled at. */
@Serializable
data class EventEdit(
    val eventId: EventId? = null,
    val title: String? = null,
    val locationId: LocationId? = null,
    val description: Markdown? = null,
    val contact: String? = null,
    val ageMin: Int? = null,
    val cost: Float? = null,
    val links: List<ExtraLink>? = null,
    val isHost: Boolean? = null,
    val website: Url? = null,
    override val image: Image? = null,
    val startTime: LocalTime? = null,
    val endTime: LocalTime? = null,
    val date: LocalDate? = null,
    val timeZoneId: String? = null,
): RecordEdit {
    override val label get() = title ?: "Unnamed Event"
    override val recordType get() = RecordType.Event
    val isFree get() = cost == 0f
    val timeZone
        get() = try {
            timeZoneId?.let { TimeZone.of(it) }
        } catch (_: Exception) { // fails in browser
            TimeZone.currentSystemDefault()
        }

    /** The start of the event, from its date, start time and time zone, or `null` when any is missing. */
    val startsAt get() = startTime?.let { timeZone?.let { date?.atTime(startTime)?.toInstant(it) } }
    /** The end of the event, from its date, end time and time zone, or `null` when any is missing. */
    val endsAt get() = endTime?.let { timeZone?.let { date?.atTime(endTime)?.toInstant(it) } }

    /** The event's links as shown: its website, a tickets link when it has a cost, and its other links. */
    val displayedLinks by lazy {
        buildList {
            website?.let { url ->
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
            if (startTime == null) add(EventProperty.StartTime)
            if (date == null) add(EventProperty.Date)
            if (cost == null) add(EventProperty.Cost)
        }.toValidityCheck()
    }

    /** True when the event is missing an image, a website, or a description of at least 100 characters. */
    val needsReview get() = image == null || website == null || description == null || description.length < 100
}

/** The keys of the parts of an [EventEdit] its validity checks. */
object EventProperty {
    val Title = "title"
    val StartTime = "start time"
    val Date = "date"
    val Cost = "cost"
}

/** An edit of this event, starting from its current values. */
fun Event.toEdit() = EventEdit(
    eventId = eventId,
    title = title,
    locationId = locationId,
    image = image,
    description = description,
    contact = contact,
    ageMin = ageMin,
    cost = cost,
    links = links,
    website = website,
    startTime = startsAt?.toLocalDateTime(timeZone)?.time,
    date = startsAt?.toLocalDateTime(timeZone)?.date,
    timeZoneId = timeZone.id,
)

/** This edit, with any field it lacks taken from [other]. */
fun EventEdit.mergeLeft(other: EventEdit?) = other?.let {
    EventEdit(
        eventId = eventId ?: it.eventId,
        title = title?.takeIf { it.isNotBlank() } ?: it.title,
        locationId = locationId ?: it.locationId,
        image = image ?: it.image,
        description = description ?: it.description,
        contact = contact ?: it.contact,
        ageMin = ageMin ?: it.ageMin,
        cost = cost ?: it.cost,
        isHost = isHost ?: it.isHost,
        links = links ?: it.links,
        website = website ?: it.website,
        date = date ?: it.date,
        startTime = startTime ?: it.startTime,
        endTime = endTime ?: it.endTime,
        timeZoneId = timeZoneId ?: it.timeZoneId,
    )
} ?: this

/** [other], with any field it lacks taken from this edit. */
fun EventEdit.mergeRight(other: EventEdit?) = other?.mergeLeft(this) ?: this