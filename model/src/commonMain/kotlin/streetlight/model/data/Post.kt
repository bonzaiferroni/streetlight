package streetlight.model.data

import kampfire.api.Slug
import kampfire.model.GeoPoint
import kampfire.model.ScaledImageArray
import kampfire.model.Url
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class Post(
    override val postId: PostId,
    override val galaxyId: GalaxyId,
    override val slug: Slug,
    override val username: String?,
    override val userThumb: Url?,
    override val title: String,
    val subtitle: String?,
    override val text: String?,
    override val geoPoint: GeoPoint?,
    val imageRef: Url?,
    override val images: ScaledImageArray?,
    override val links: List<ExtraLink>?,
    override val createdAt: Instant,
    override val updatedAt: Instant,
): GalaxyPost {

    override val boosts: Int get() = 0
    override val description get() = text
    override val postType get() = PostType.Content
}
