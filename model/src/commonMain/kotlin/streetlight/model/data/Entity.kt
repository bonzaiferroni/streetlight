package streetlight.model.data

import kampfire.api.Markdown
import kampfire.api.Slug
import kampfire.api.Username
import kampfire.api.toMarkdown
import kabinet.utils.toAgoFormat
import kampfire.model.GeoPoint
import kampfire.model.Url
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
    /** A summary of the entity, built from its other properties when it has no [description]. */
    val body: Markdown
    val description: Markdown? get() = null
    val url: Url? get() = null
    val links: List<ExtraLink>? get() = null
    val createdAt: Instant? get() = null
}

/** The kinds of record whose whole [Entity] is read by its slug, through `Api.Entities.Read`. */
enum class EntityType { Location, Media, Event }

/** The record of [type] at [slug], whose whole [Entity] is read through `Api.Entities.Read`. */
data class EntityRef(val type: EntityType, val slug: Slug)

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
    override val description: Markdown? = null,
    override val body: Markdown = description ?: label.toMarkdown(),
    val route: AppRoute? = null,
    override val links: List<ExtraLink>? = null,
    override val createdAt: Instant? = null,
    val recordType: RecordType? = null,
): Entity


