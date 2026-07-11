package streetlight.model.data

import kampfire.api.Markdown
import kampfire.api.Slug
import kampfire.api.Username
import kampfire.model.GeoPoint
import kampfire.model.Labeled
import kampfire.model.ScaledImageArray
import kampfire.model.Url
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable
data class Media(
    val mediaId: MediaId,
    val slug: Slug,
    val username: Username,
    val mediaType: MediaType,
    val title: String?,
    val subtitle: String?,
    val text: Markdown?,
    val link: Url?,
    override val geoPoint: GeoPoint?,
    val imageRef: Url?,
    override val images: ScaledImageArray?,
    val updatedAt: Instant,
    val createdAt: Instant,
): Entity {
    override val label get() = title ?: "(untitled)"
    override val sublabel get() = subtitle
    override val body get() = text
    override val links get() = link?.let { listOf(ExtraLink("link", it.value)) } // deep mystery: why isn't extralink's url property an Url
}

@Serializable
@JvmInline
value class MediaId(override val value: Uuid): RecordId {
    companion object {
        fun random() = MediaId(Uuid.random())
    }
}

@Serializable
data class MediaPost(
    val media: Media,
    override val base: Post,
): GalaxyPost {
    override val images get() = media.images
    override val geoPoint get() = media.geoPoint
    override val label get() = media.title ?: "Untitled"
    override val sublabel get() = media.subtitle
    override val body get() = media.text
    override val links get() = media.link?.let { listOf(ExtraLink("link", it.value)) }

}

enum class MediaType(label: String? = null): Labeled {
    Text,
    Image;
    // Audio,
    // Link,
    // Gallery
    // News,

    override val label = label ?: name
}