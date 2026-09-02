package kampfire.model

import kotlinx.serialization.Serializable
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable
data class RecordCursor(
    val recordId: Uuid,
    val recordAt: Instant,
    val limit: Int = DefaultLimit,
) {
    companion object {
        val DefaultLimit = 30
    }
}

val RecordCursor?.limitOrDefault get() = this?.limit ?: RecordCursor.DefaultLimit

data class CursorStatus(
    val isFetching: Boolean = false,
    val isComplete: Boolean = false,
)

suspend fun <T, K> requestWithCursor(
    cursorState: MutableTap<CursorStatus>,
    list: LiveList<T, K>,
    cursorOf: (T) -> RecordCursor,
    requester: suspend (RecordCursor?) -> List<T>?
) {
    val status = cursorState.now
    if (status.isComplete || status.isFetching) return
    println("fetching")
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