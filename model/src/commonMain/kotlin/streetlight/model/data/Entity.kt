package streetlight.model.data

import kampfire.api.Markdown
import kampfire.api.Username
import kampfire.model.GeoPoint
import koala.Image
import koala.html.AppRoute
import koala.model.MarkerId
import kotlinx.serialization.Serializable
import kotlin.time.Instant

/**
 * Content shown in a feed, a header, or anywhere else an entity appears.
 *
 * Its members name general content. A type maps its own fields onto them and leaves a member it does not hold at
 * its default.
 */
@Serializable
sealed interface Entity {
    val label: String
    val geoPoint: GeoPoint?
    val markerId: MarkerId?
    val post: Post? get() = null
    val username: Username? get() = null
    val heading: String get() = label
    val image: Image? get() = null
    val sublabel: String? get() = null
    val body: Markdown? get() = null
    val links: List<ExtraLink>? get() = null
    val createdAt: Instant? get() = null
}

/** An [Entity] built directly, for content that has no type of its own, such as a preview of [recordType]. */
@Serializable
data class CustomEntity(
    override val label: String,
    override val markerId: MarkerId? = null,
    override val geoPoint: GeoPoint? = null,
    override val username: Username? = null,
    override val heading: String = label,
    override val image: Image? = null,
    override val sublabel: String? = null,
    override val body: Markdown? = null,
    val route: AppRoute? = null,
    override val links: List<ExtraLink>? = null,
    override val createdAt: Instant? = null,
    val recordType: RecordType? = null,
): Entity

/** A page of a feed, with the marks and tallies of its posts and the cursor of the next page. */
@Serializable
data class EntityFeed(
    val entities: List<Entity>,
    val marks: Map<GalaxyId, List<GalaxyMark>>? = null,
    val tallies: Map<PostId, List<MarkTally>>? = null,
    val nextCursor: EntityCursor? = null,
) {
    /** True when this is the last page. */
    val isCompleted get() = nextCursor == null

    /** The curator status of [entity] when it is a post in a galaxy with marks, or `null`. */
    fun curatorOf(entity: Entity): CuratorStatus? {
        val postId = entity.post?.postId ?: return null
        val galaxyId = entity.post?.galaxy?.galaxyId ?: return null
        val galaxyMarks = marks?.get(galaxyId) ?: return null
        return curatorStatusOf(postId, galaxyMarks, (tallies ?: return null)[postId])
    }
}