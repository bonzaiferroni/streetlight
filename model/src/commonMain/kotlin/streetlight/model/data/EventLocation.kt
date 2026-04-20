package streetlight.model.data

import kampfire.model.GeoPoint
import kampfire.model.ScaledImageArray
import kampfire.model.Url
import kampfire.model.toUrl
import kotlin.time.Instant
import kotlinx.serialization.Serializable
import kotlin.time.Duration.Companion.hours

@Serializable
data class EventLocation(
    val eventId: EventId,
    val locationId: LocationId,
    val slug: Slug,
    val username: String,
    val url: String?,
    val eventImages: ScaledImageArray?,
    val title: String,
    val description: String?,
    val cost: Float?,
    val status: EventStatus,
    val visibility: Int,
    val geoPoint: GeoPoint,
    val eventLinks: List<ExtraLink>?,
    val locationName: String,
    val locationDescription: String?,
    val address: String?,
    val city: String?,
    val locationImages: ScaledImageArray?,
    val startsAt: Instant,
    val endsAt: Instant?,
    val updatedAt: Instant,
    val createdAt: Instant,
) {
    val links by lazy {
        buildList {
            url?.let { url ->
                add(ExtraLink("website", url))
                cost?.takeIf { it > 0 }?.let {
                    add(ExtraLink("tickets", url))
                }
            }
            eventLinks?.let {
                addAll(it)
            }
        }.takeIf { it.isNotEmpty() }
    }

    val images get() = eventImages ?: locationImages

    val addressLine by lazy {
        addressLineOf(address, city)
    }

    val endsAtOrLater get() = endsAt ?: (startsAt + 4.hours)
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
