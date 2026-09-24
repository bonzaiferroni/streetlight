package streetlight.model.data

import kampfire.api.Markdown
import kampfire.api.Slug
import kampfire.api.Username
import kotlinx.serialization.Serializable
import kotlin.time.Instant

/** A post sharing a record to a galaxy, with its lean and light count. */
@Serializable
data class Post(
    val postId: PostId,
    val galaxy: GalaxyTrace,
    val postType: PostType,
    val username: Username?,
    val title: String?,
    val text: Markdown?,
    val lightCount: Int,
    val lean: Int?,
    val markCount: Int?,
    val createdAt: Instant,
    val updatedAt: Instant,
)

/** The name and slug of the galaxy a post belongs to. */
@Serializable
data class GalaxyTrace(
    val galaxyId: GalaxyId,
    val name: String,
    val slug: Slug
)
