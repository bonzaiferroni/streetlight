package streetlight.model.data

import kampfire.api.Markdown
import kampfire.api.Slug
import kampfire.api.Username
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class Post(
    val postId: PostId,
    val galaxy: GalaxyTrace,
    val postType: PostType,
    val username: Username?,
    val title: String?,
    val text: Markdown?,
    val lean: Lean?,
    val lightCount: Int,
    val createdAt: Instant,
    val updatedAt: Instant,
)

@Serializable
data class GalaxyTrace(
    val galaxyId: GalaxyId,
    val name: String,
    val slug: Slug
)