package streetlight.model.data

import kampfire.api.Markdown
import kampfire.api.Slug
import kampfire.api.Username
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class Post(
    val postId: PostId,
    val galaxyId: GalaxyId,
    val postType: PostType,
    val galaxyName: String,
    val galaxySlug: Slug,
    val username: Username?,
    val title: String?,
    val text: Markdown?,
    val isLit: Boolean,
    val lightCount: Int,
    val createdAt: Instant,
    val updatedAt: Instant,
)