package streetlight.model.data

import kampfire.model.Url
import kotlinx.serialization.Serializable

@Serializable
sealed interface SelectorSchema {
    val schemaType: SchemaType
}

@Serializable
data class EventFeedSchema(
    val event: String? = null,
    val feedLocation: String? = null,
    val address: String? = null,
    val title: String? = null,
    val eventLocation: String? = null,
    val link: String? = null,
    val image: String? = null,
    val cost: String? = null,
    val description: String? = null,
    val date: String? = null,
    val time: String? = null,
): SelectorSchema {
    override val schemaType get() = SchemaType.EventFeed
}

@Serializable
data class EventPageSchema(
    val title: String? = null,
    val location: String? = null,
    val address: String? = null,
    val image: String? = null,
    val cost: String? = null,
    val description: String? = null,
    val date: String? = null,
    val startTime: String? = null,
    val endTime: String? = null,
    val ageMin: String? = null,
    val contact: String? = null,
): SelectorSchema {
    override val schemaType get() = SchemaType.EventPage
}

@Serializable
data class UrlSchemas(
    val locationId: LocationId,
    val url: Url,
    val schemas: List<SelectorSchema>
)