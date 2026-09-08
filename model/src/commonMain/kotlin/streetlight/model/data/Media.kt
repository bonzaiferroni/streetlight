package streetlight.model.data

import kampfire.api.Markdown
import kampfire.api.Slug
import kampfire.api.Username
import kampfire.model.GeoPoint
import kampfire.model.Labeled
import kampfire.model.Url
import koala.Image
import koala.model.RouteContent
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable
data class Media(
    val mediaId: MediaId,
    val slug: Slug,
    override val username: Username,
    val mediaType: MediaType,
    val title: String?,
    val subtitle: String?,
    val text: Markdown?,
    val link: Url?,
    override val geoPoint: GeoPoint?,
    override val image: Image?,
    override val design: PageDesign?,
    val updatedAt: Instant,
    override val createdAt: Instant,
): FeedEntity, RouteContent, DesignContent {
    override val label get() = title ?: "(untitled)"
    override val sublabel get() = subtitle
    override val body get() = text
    override val links get() = link?.let { listOf(ExtraLink("link", it)) }
}

@Serializable
@JvmInline
value class MediaId(override val value: Uuid): RecordId {
    companion object {
        fun random() = MediaId(Uuid.random())
    }
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