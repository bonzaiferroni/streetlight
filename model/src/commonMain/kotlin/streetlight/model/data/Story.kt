package streetlight.model.data

import kampfire.model.GeoPoint
import kampfire.utils.randomUuidString
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
data class Story(
    val storyId: StoryId,
    val headline: String,
    val url: String,
    val imageUrl: String,
    val iconUrl: String,
    val description: String,
    val location: GeoPoint,
    val visibility: Int,
    val postedAt: Instant,
    val createdAt: Instant,
    val updatedAt: Instant,
)

@JvmInline
@Serializable
value class StoryId(override val value: String): ProjectId {
    companion object { fun random() = StoryId(randomUuidString())}
}

@Serializable
data class StoryUpdate(
    val headline: String = "",
    val infoUrl: String? = null,
    val imageUrl: String? = null,
    val description: String = "",
    val location: GeoPoint? = null,
    val postedAt: Instant? = null,
)

@Serializable
data class StoryParse(
    val headline: String,
    val imageUrl: String? = null,
    val description: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val postedAt: Instant,
) {
    fun toStoryUpdate() = StoryUpdate(
        headline = headline,
        imageUrl = imageUrl,
        description = description,
        location = if (latitude != null && longitude != null) GeoPoint(latitude, longitude) else null,
        postedAt = postedAt
    )
}