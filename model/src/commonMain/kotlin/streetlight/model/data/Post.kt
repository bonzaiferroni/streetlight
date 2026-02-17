package streetlight.model.data

import kampfire.model.GeoPoint
import kampfire.utils.randomUuidString
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
data class Post(
    val postId: PostId,
    val title: String,
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
value class PostId(override val value: String): ProjectId {
    companion object { fun random() = PostId(randomUuidString())}
}

@Serializable
data class PostUpdate(
    val title: String = "",
    val infoUrl: String? = null,
    val imageUrl: String? = null,
    val description: String = "",
    val location: GeoPoint? = null,
    val postedAt: Instant? = null,
)

@Serializable
data class StoryParse(
    val title: String,
    val imageUrl: String? = null,
    val description: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val postedAt: Instant,
) {
    fun toStoryUpdate() = PostUpdate(
        title = title,
        imageUrl = imageUrl,
        description = description,
        location = geoPoint,
        postedAt = postedAt
    )

    val geoPoint: GeoPoint? get() = if (latitude != null && longitude != null) GeoPoint(longitude, latitude) else null
}