package streetlight.model

import kampfire.api.EndpointParam
import kampfire.api.PathBuilder
import streetlight.model.data.EntityCursor
import streetlight.model.data.SortDirection
import kotlin.time.Instant
import kotlin.uuid.Uuid

/**
 * An endpoint that pages its feed with an [EntityCursor].
 *
 * The cursor's direction is read from [directionParam], or fixed at [defaultDirection] without one. Its tag is read
 * from [tagParam], and an endpoint without one is not filtered by tag.
 */
interface CursorEndpoint {
    val recordIdParam: EndpointParam<Uuid>
    val directionParam: EndpointParam<SortDirection>? get() = null
    val tagParam: EndpointParam<Int>? get() = null
    val defaultDirection: SortDirection get() = SortDirection.Descending
}

/** An endpoint that pages its feed with an [EntityCursor.Time]. */
interface TimeCursorEndpoint: CursorEndpoint {
    val recordAtParam: EndpointParam<Instant>
}

/** An endpoint that pages its feed with an [EntityCursor.Score]. */
interface ScoreCursorEndpoint: CursorEndpoint {
    val scoreParam: EndpointParam<Double>
}

/** An endpoint that pages its feed with an [EntityCursor.Mark]. */
interface MarkCursorEndpoint: CursorEndpoint {
    val markIdParam: EndpointParam<Uuid>
    val countParam: EndpointParam<Int>
}

/** Writes the parts of [cursor] as the parameters of [endpoint], or nothing when it is `null`. */
fun PathBuilder.writeTimeCursor(endpoint: TimeCursorEndpoint, cursor: EntityCursor.Time?) {
    if (cursor == null) return
    writeBaseCursor(endpoint, cursor)
    cursor.recordAt?.let { writeParam(endpoint.recordAtParam, it) }
}

/** Writes the parts of [cursor] as the parameters of [endpoint], or nothing when it is `null`. */
fun PathBuilder.writeScoreCursor(endpoint: ScoreCursorEndpoint, cursor: EntityCursor.Score?) {
    if (cursor == null) return
    writeBaseCursor(endpoint, cursor)
    cursor.score?.let { writeParam(endpoint.scoreParam, it) }
}

/** Writes the parts of [cursor] as the parameters of [endpoint], or nothing when it is `null`. */
fun PathBuilder.writeMarkCursor(endpoint: MarkCursorEndpoint, cursor: EntityCursor.Mark?) {
    if (cursor == null) return
    writeBaseCursor(endpoint, cursor)
    writeParam(endpoint.markIdParam, cursor.markId.value)
    cursor.count?.let { writeParam(endpoint.countParam, it) }
}

private fun PathBuilder.writeBaseCursor(endpoint: CursorEndpoint, cursor: EntityCursor) {
    cursor.recordId?.let { writeParam(endpoint.recordIdParam, it) }
    endpoint.directionParam?.let { writeParam(it, cursor.direction) }
    endpoint.tagParam?.let { param -> cursor.tag?.let { writeParam(param, it) } }
}

/** Writes the parts of [cursor], of any kind, as the parameters of [endpoint], or nothing when it is `null`. */
fun <T> PathBuilder.writeCursor(endpoint: T, cursor: EntityCursor?)
        where T: TimeCursorEndpoint, T: ScoreCursorEndpoint, T: MarkCursorEndpoint {
    when (cursor) {
        null -> return
        is EntityCursor.Time -> writeTimeCursor(endpoint, cursor)
        is EntityCursor.Score -> writeScoreCursor(endpoint, cursor)
        is EntityCursor.Mark -> writeMarkCursor(endpoint, cursor)
    }
}
