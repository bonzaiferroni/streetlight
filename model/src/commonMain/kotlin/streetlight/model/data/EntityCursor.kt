package streetlight.model.data

import kotlinx.serialization.Serializable
import kotlin.time.Instant
import kotlin.uuid.Uuid

/** The direction a feed is sorted in. */
@Serializable
enum class SortDirection { Ascending, Descending }

/**
 * Pages an [EntityFeed]: the sort value and [recordId] of the last entity on a page, so the next page starts
 * after it.
 *
 * It pages any table keyed by a [Uuid]. A [tag], the ordinal of an [EventTag], filters the feed to entities with that
 * tag.
 */
@Serializable
sealed interface EntityCursor {
    val recordId: Uuid?
    val direction: SortDirection
    val tag: Int?

    @Serializable
    data class Time(
        override val direction: SortDirection,
        override val recordId: Uuid? = null,
        val recordAt: Instant? = null,
        override val tag: Int? = null,
    ) : EntityCursor

    @Serializable
    data class Score(
        override val direction: SortDirection,
        override val recordId: Uuid? = null,
        val score: Double? = null,
        override val tag: Int? = null,
    ) : EntityCursor {
        companion object  {
            val Default get() = Score(SortDirection.Descending)
        }
    }

    @Serializable
    data class Mark(
        val markId: MarkId,
        override val recordId: Uuid? = null,
        val count: Int? = null,
        override val tag: Int? = null,
    ) : EntityCursor {
        override val direction get() = SortDirection.Descending
    }

    companion object {
        val DefaultLimit = 30
        val MapLimit = 100
        val Default = Time(SortDirection.Descending)
        val Upcoming = Time(SortDirection.Ascending)
    }
}