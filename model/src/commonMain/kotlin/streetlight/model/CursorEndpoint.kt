package streetlight.model

import kampfire.api.EndpointParam
import kampfire.api.PathBuilder
import streetlight.model.data.MapQuery
import streetlight.model.data.MapQueryLegacy
import streetlight.model.data.EntityCursor
import streetlight.model.data.SortDirection
import kotlin.time.Instant
import kotlin.uuid.Uuid

interface CursorEndpoint {
    val recordId: EndpointParam<Uuid>
    val markId: EndpointParam<Uuid>
    val count: EndpointParam<Int>
    val direction: EndpointParam<SortDirection>
    val lean: EndpointParam<Int>
    val recordAt: EndpointParam<Instant>
}

fun PathBuilder.writeCursor(endpoint: CursorEndpoint, cursor: EntityCursor?) {
    if (cursor == null) return
    cursor.recordId?.let { writeParam(endpoint.recordId, it) }
    when (cursor) {
        is EntityCursor.Mark -> {
            writeParam(endpoint.markId, cursor.markId.value)
            cursor.count?.let { writeParam(endpoint.count, it) }
        }
        is EntityCursor.Lean -> {
            writeParam(endpoint.direction, cursor.direction)
            cursor.postLean?.let { writeParam(endpoint.lean, it) }
        }
        is EntityCursor.Time -> {
            writeParam(endpoint.direction, cursor.direction)
            cursor.recordAt?.let { writeParam(endpoint.recordAt, it) }
        }
    }
}