package streetlight.model.data

import kotlinx.serialization.Serializable
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable
enum class SortDirection { Ascending, Descending }

@Serializable
sealed interface PostCursor {
    val postId: PostId?
    val direction: SortDirection

    @Serializable
    data class Time(
        override val direction: SortDirection,
        override val postId: PostId? = null,
        val recordAt: Instant? = null,
    ) : PostCursor

    @Serializable
    data class Lean(
        override val direction: SortDirection,
        override val postId: PostId? = null,
        val postLean: Int? = null,
    ) : PostCursor

    @Serializable
    data class Mark(
        val markId: MarkId,
        override val postId: PostId? = null,
        val count: Int? = null,
    ) : PostCursor {
        override val direction get() = SortDirection.Descending
    }

    companion object {
        val DefaultLimit = 30
        val Default = Time(SortDirection.Descending)
    }
}