package streetlight.model

import kampfire.api.EndpointParam
import kampfire.api.PathBuilder
import streetlight.model.data.PostCursor
import streetlight.model.data.SortDirection
import kotlin.time.Instant
import kotlin.uuid.Uuid

interface CursorEndpoint {
    val postId: EndpointParam<Uuid>
    val markId: EndpointParam<Uuid>
    val count: EndpointParam<Int>
    val direction: EndpointParam<SortDirection>
    val lean: EndpointParam<Int>
    val recordAt: EndpointParam<Instant>
}

fun PathBuilder.writeCursor(endpoint: CursorEndpoint, cursor: PostCursor?) {
    if (cursor == null) return
    cursor.postId?.let { writeParam(endpoint.postId, it.value) }
    when (cursor) {
        is PostCursor.Mark -> {
            writeParam(endpoint.markId, cursor.markId.value)
            cursor.count?.let { writeParam(endpoint.count, it) }
        }
        is PostCursor.Lean -> {
            writeParam(endpoint.direction, cursor.direction)
            cursor.postLean?.let { writeParam(endpoint.lean, it) }
        }
        is PostCursor.Time -> {
            writeParam(endpoint.direction, cursor.direction)
            cursor.recordAt?.let { writeParam(endpoint.recordAt, it) }
        }
    }
}