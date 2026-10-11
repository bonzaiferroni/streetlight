package streetlight.model.data

import kabinet.utils.toAgoFormat
import kampfire.api.Username
import kampfire.api.toMarkdown
import kotlinx.serialization.Serializable
import kotlin.time.Instant

/**
 * A page of a feed, with the marks and tallies of its posts and the cursor of the next page.
 *
 * [source] names the feed the next page is read from, and is absent from a feed no section pages, such as the map's.
 * [types] are the types of feed its context offers this viewer, for its section's menu; every type of the context
 * when absent.
 */
@Serializable
data class EntityFeed(
    val entities: List<Entity>,
    val marks: Map<GalaxyId, List<GalaxyMark>>? = null,
    val tallies: Map<PostId, List<MarkTally>>? = null,
    val nextCursor: EntityCursor? = null,
    val source: FeedSource? = null,
    val types: List<FeedType>? = null,
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

/** A body that tells when an entity was added at [createdAt]. */
fun addedBodyOf(createdAt: Instant) = "Added ${createdAt.toAgoFormat()}".toMarkdown()

/** The feed an [EntityFeed] pages through: the [type] of feed shown in [context]. */
@Serializable
data class FeedSource(val context: FeedContext, val type: FeedType)

/** The page at [cursor] of the feed from [source]. */
@Serializable
data class FeedRequest(val source: FeedSource, val cursor: EntityCursor)

/** Where a feed is shown, with the id its reads need. */
@Serializable
sealed interface FeedContext {
    /** The types of feed this context offers. */
    val types: List<FeedType>

    @Serializable
    data object Home: FeedContext {
        override val types get() = listOf(FeedType.Events, FeedType.Media, FeedType.GalaxyPosts)
    }

    @Serializable
    data class City(val cityId: CityId): FeedContext {
        override val types get() = listOf(FeedType.Events, FeedType.Locations)
    }

    @Serializable
    data class Location(val locationId: LocationId): FeedContext {
        override val types get() = listOf(FeedType.Events)
    }

    @Serializable
    data class Galaxy(val galaxyId: GalaxyId): FeedContext {
        override val types get() = listOf(FeedType.Posts)
    }

    @Serializable
    data class Star(val username: Username): FeedContext {
        override val types get() = listOf(FeedType.Media)
    }
}

/**
 * What a feed lists: upcoming events, locations newest first, the posts of the galaxies a star follows, a galaxy's
 * posts, or media.
 */
@Serializable
enum class FeedType { Events, Locations, GalaxyPosts, Posts, Media }