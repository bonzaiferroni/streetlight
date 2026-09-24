package kampfire.model

import kotlinx.serialization.Serializable
import kotlin.time.Instant
import kotlin.uuid.Uuid

/** A cursor paging records by time, holding the last record read and the page size. */
@Serializable
data class TimeCursor(
    val recordId: Uuid,
    val recordAt: Instant,
    val limit: Int = DefaultLimit,
) {
    companion object {
        val DefaultLimit = 30
    }
}

/** The page size, or [TimeCursor.DefaultLimit] when there is no cursor. */
val TimeCursor?.limitOrDefault get() = this?.limit ?: TimeCursor.DefaultLimit

/** Whether a paged list is fetching or has reached its end. */
data class CursorStatus(
    val isFetching: Boolean = false,
    val isComplete: Boolean = false,
)

/**
 * Fetches the next page after the last item of [list] and adds it, updating [cursorState].
 *
 * Does nothing while a fetch is running or after the end is reached. A page shorter than the limit marks the
 * end.
 */
suspend fun <T, K> requestWithCursor(
    cursorState: MutableTap<CursorStatus>,
    list: LiveList<T, K>,
    cursorOf: (T) -> TimeCursor,
    requester: suspend (TimeCursor?) -> List<T>?
) {
    val status = cursorState.now
    if (status.isComplete || status.isFetching) return
    cursorState.set { copy(isFetching = true) }
    val cursor = list.liveItems.lastOrNull()?.let { cursorOf(it) }

    val items = requester(cursor)
    if (items == null) {
        cursorState.set { CursorStatus(false) }
        return
    }

    val isCompleted = items.size < cursor.limitOrDefault
    cursorState.set { CursorStatus(false, isCompleted) }
    list.add(items)
}