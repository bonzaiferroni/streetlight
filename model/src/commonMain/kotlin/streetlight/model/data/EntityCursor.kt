package streetlight.model.data

import kotlinx.serialization.Serializable
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable
enum class SortDirection { Ascending, Descending }

@Serializable
sealed interface EntityCursor {
    val recordId: Uuid?
    val direction: SortDirection

    @Serializable
    data class Time(
        override val direction: SortDirection,
        override val recordId: Uuid? = null,
        val recordAt: Instant? = null,
    ) : EntityCursor

    @Serializable
    data class Lean(
        override val direction: SortDirection,
        override val recordId: Uuid? = null,
        val postLean: Int? = null,
    ) : EntityCursor {
        companion object  {
            val Default get() = Lean(SortDirection.Descending)
        }
    }

    @Serializable
    data class Mark(
        val markId: MarkId,
        override val recordId: Uuid? = null,
        val count: Int? = null,
    ) : EntityCursor {
        override val direction get() = SortDirection.Descending
    }

    companion object {
        val DefaultLimit = 30
        val Default = Time(SortDirection.Descending)
    }
}