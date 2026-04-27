package streetlight.model.data

import kampfire.model.GeoPoint
import kampfire.model.Url
import kampfire.model.toUrl
import kampfire.utils.randomUuidString
import kotlin.time.Instant
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
data class ProtoPost(
    val postId: ProtoPostId,
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
value class ProtoPostId(override val value: String): ProjectId {
    companion object { fun random() = ProtoPostId(randomUuidString())}
}

@Serializable
data class ProtoPostUpdate(
    val title: String = "",
    val infoUrl: String? = null,
    val imageUrl: Url? = null,
    val description: String = "",
    val location: GeoPoint? = null,
    val postedAt: Instant? = null,
)

@Serializable
data class ProtoStoryParse(
    val title: String,
    val imageUrl: String? = null,
    val description: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val postedAt: Instant,
) {
    fun toStoryUpdate() = ProtoPostUpdate(
        title = title,
        imageUrl = imageUrl?.toUrl(),
        description = description,
        location = geoPoint,
        postedAt = postedAt
    )

    val geoPoint: GeoPoint? get() = if (latitude != null && longitude != null) GeoPoint(longitude, latitude) else null
}