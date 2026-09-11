package streetlight.model.data

import kampfire.api.Markdown
import kampfire.api.Username
import kampfire.model.GeoPoint
import koala.Image
import koala.html.AppRoute
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
sealed interface FeedEntity {
    val label: String
    val geoPoint: GeoPoint?
    val post: Post? get() = null
    val username: Username? get() = null
    val heading: String get() = label
    val image: Image? get() = null
    val sublabel: String? get() = null
    val body: Markdown? get() = null
    val links: List<ExtraLink>? get() = null
    val createdAt: Instant? get() = null
}

@Serializable
data class CustomEntity(
    override val label: String,
    override val geoPoint: GeoPoint? = null,
    override val username: Username? = null,
    override val heading: String = label,
    override val image: Image? = null,
    override val sublabel: String? = null,
    override val body: Markdown? = null,
    val route: AppRoute? = null,
    override val links: List<ExtraLink>? = null,
    override val createdAt: Instant? = null,
): FeedEntity

@Serializable
data class EntityFeed(
    val entities: List<FeedEntity>,
    val marks: Map<GalaxyId, List<GalaxyMark>>? = null,
    val tallies: Map<PostId, List<MarkTally>>? = null,
    val nextCursor: PostCursor? = null,
) {
    val isCompleted get() = entities.size >= PostCursor.DefaultLimit

    fun curatorOf(entity: FeedEntity): CuratorStatus? {
        val postId = entity.post?.postId ?: return null
        val galaxyId = entity.post?.galaxy?.galaxyId ?: return null
        val galaxyMarks = marks?.get(galaxyId) ?: return null
        return curatorStatusOf(postId, galaxyMarks, (tallies ?: return null)[postId])
    }
}