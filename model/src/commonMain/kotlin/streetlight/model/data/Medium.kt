package streetlight.model.data

import kampfire.api.Markdown
import kampfire.api.Slug
import kampfire.api.Username
import kampfire.model.GeoPoint
import kampfire.model.ScaledImageArray
import kampfire.model.Url
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable
data class Medium(
    val mediumId: MediumId,
    val slug: Slug,
    val username: Username,
    val mediaType: MediaType,
    val title: String?,
    val subtitle: String?,
    val text: Markdown?,
    val link: Url?,
    val geoPoint: GeoPoint?,
    val imageRef: Url?,
    val images: ScaledImageArray?,
    val updatedAt: Instant,
    val createdAt: Instant,
) {
}

@Serializable
@JvmInline
value class MediumId(override val value: Uuid): RecordId {
    companion object {
        fun random() = MediumId(Uuid.random())
    }
}

@Serializable
data class MediumPost(
    val medium: Medium,
    override val base: Post,
): GalaxyPost {
    override val images get() = medium.images
    override val geoPoint get() = medium.geoPoint
    override val label get() = medium.title ?: "Untitled"
    override val sublabel get() = medium.subtitle
    override val body get() = medium.text
    override val links get() = medium.link?.let { listOf(ExtraLink("link", it.value)) }

}

enum class MediaType {
    Text,
    Image,
    // Audio,
    // Link,
    // Gallery
    // News,
}