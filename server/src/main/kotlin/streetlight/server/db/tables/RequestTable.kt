package streetlight.server.db.tables

import kabinet.utils.toInstantFromUtc
import kabinet.utils.toLocalDateTimeUtc
import klutch.utils.toStringId
import klutch.utils.toUUID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import streetlight.model.data.EventId
import streetlight.model.data.Request
import streetlight.model.data.RequestId
import streetlight.model.data.SongId
import streetlight.server.utils.toProjectId

object RequestTable : UUIDTable() {
    val eventId = reference("event_id", EventTable, onDelete = ReferenceOption.CASCADE)
    val songId = reference("song_id", SongTable, onDelete = ReferenceOption.CASCADE)
    val performed = bool("performed")
    val notes = text("notes")
    val requesterName = text("requester_name").nullable()
    val requestedAt = datetime("requested_at")
}

fun ResultRow.toRequest() = Request(
    requestId = toProjectId(RequestTable.id),
    eventId = toProjectId(RequestTable.eventId),
    songId = toProjectId(RequestTable.songId),
    performed = this[RequestTable.performed],
    notes = this[RequestTable.notes],
    requesterName = this[RequestTable.requesterName],
    requestedAt = this[RequestTable.requestedAt].toInstantFromUtc(),
)

// Updaters
fun UpdateBuilder<*>.writeFull(request: Request) {
    this[RequestTable.id] = request.requestId.toUUID()
    this[RequestTable.eventId] = request.eventId.toUUID()
    this[RequestTable.songId] = request.songId.toUUID()
    writeUpdate(request)
}

fun UpdateBuilder<*>.writeUpdate(request: Request) {
    this[RequestTable.performed] = request.performed
    this[RequestTable.notes] = request.notes
    this[RequestTable.requesterName] = request.requesterName
    this[RequestTable.requestedAt] = request.requestedAt.toLocalDateTimeUtc()
}