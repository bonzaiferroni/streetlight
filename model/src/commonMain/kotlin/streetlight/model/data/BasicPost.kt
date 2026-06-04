package streetlight.model.data

import kampfire.api.Slug
import kampfire.model.GeoPoint
import kampfire.model.ScaledImageArray
import kampfire.model.Url
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class BasicPost(
    override val postId: PostId,
    override val galaxyId: GalaxyId,
    override val slug: Slug,
    override val galaxyName: String,
    override val galaxySlug: Slug,
    override val username: String?,
    override val label: String,
    override val sublabel: String?,
    override val text: String?,
    override val geoPoint: GeoPoint?,
    val imageRef: Url?,
    override val images: ScaledImageArray?,
    override val isLit: Boolean,
    override val lightCount: Int,
    override val links: List<ExtraLink>?,
    override val createdAt: Instant,
    override val updatedAt: Instant,
): GalaxyPost {

    override val description get() = text
    override val postType get() = PostType.Content
}
