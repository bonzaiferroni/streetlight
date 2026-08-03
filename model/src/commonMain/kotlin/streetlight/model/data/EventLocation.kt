package streetlight.model.data

import kampfire.api.Markdown
import kampfire.api.Slug
import kampfire.api.Username
import kampfire.model.GeoPoint
import kampfire.model.Labeled
import kampfire.model.Url
import koala.Image
import koala.SiteImage
import koala.model.RouteContent
import kotlin.time.Instant
import kotlinx.serialization.Serializable
import kotlin.time.Duration.Companion.hours

@Serializable
data class EventLocation(
    val eventId: EventId,
    val locationId: LocationId,
    val eventSlug: Slug,
    val locationSlug: Slug,
    val scout: Username,
    val url: Url?,
    val eventImage: Image?,
    val title: String,
    val description: Markdown?,
    val cost: Float?,
    val status: EventStatus,
    val visibility: Int,
    override val geoPoint: GeoPoint,
    val eventLinks: List<ExtraLink>?,
    val locationName: String?,
    val locationDescription: Markdown?,
    val address: String?,
    val city: String?,
    val locationImage: Image?,
    val lightCount: Int?,
    val isLit: Boolean,
    val startsAt: Instant?,
    val endsAt: Instant?,
    val updatedAt: Instant,
    val createdAt: Instant,
): Entity, Labeled, RouteContent {
    override val links by lazy {
        buildList {
            url?.let { url ->
                add(ExtraLink("website", url))
            }
            eventLinks?.let {
                addAll(it)
            }
        }.takeIf { it.isNotEmpty() }
    }

    override val image get() = eventImage ?: locationImage ?: SiteImage.placeholder

    val addressLine by lazy {
        addressLineOf(address, city)
    }

    override val label get() = title
    override val sublabel get() = locationLabel
    override val body get() = description

    val locationLabel get() = locationName ?: address ?: "(geolocation)"

    // val endsAtOrLater get() = if (endsAt != null && startsAt != null) endsAt - startsAt else null
}

fun addressLineOf(address: String?, city: String?) = buildString {
    address?.let {
        append(it)
        if (city != null) {
            append(", ")
        }
    }
    city?.let {
        append(it)
    }
}.takeIf { it.isNotEmpty() }
