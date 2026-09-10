package streetlight.model.data

import kotlinx.serialization.Serializable
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable
enum class SortDirection { Ascending, Descending }

@Serializable
sealed interface PostCursor {
    val postId: PostId

    @Serializable
    data class Time(
        override val postId: PostId,
        val recordAt: Instant,
    ) : PostCursor

    @Serializable
    data class Lean(
        override val postId: PostId,
        val postLean: Double,
    ) : PostCursor

    @Serializable
    data class Mark(
        override val postId: PostId,
        val markId: MarkId,
        val count: Long,
    ) : PostCursor

    companion object {
        val DefaultLimit = 30
    }
}