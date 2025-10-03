package streetlight.server.db.tables

import kabinet.utils.toInstantFromUtc
import kabinet.utils.toLocalDateTimeUtc
import klutch.utils.toUUID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import streetlight.model.data.Request
import streetlight.server.utils.toProjectId

object RequestTable : UUIDTable() {
    val eventId = reference("event_id", EventTable, onDelete = ReferenceOption.CASCADE)
    val songId = reference("song_id", SongTable, onDelete = ReferenceOption.CASCADE)
    val isJoining = bool("is_joining")
    val comment = text("comment").nullable()
    val requesterName = text("requester_name").nullable()
    val createdAt = datetime("created_at")
}

fun ResultRow.toRequest() = Request(
    requestId = toProjectId(RequestTable.id),
    eventId = toProjectId(RequestTable.eventId),
    songId = toProjectId(RequestTable.songId),
    isJoining = this[RequestTable.isJoining],
    comment = this[RequestTable.comment],
    requesterName = this[RequestTable.requesterName],
    createdAt = this[RequestTable.createdAt].toInstantFromUtc(),
)

// Updaters
fun UpdateBuilder<*>.writeFull(request: Request) {
    this[RequestTable.id] = request.requestId.toUUID()
    this[RequestTable.eventId] = request.eventId.toUUID()
    this[RequestTable.songId] = request.songId.toUUID()
    writeUpdate(request)
}

fun UpdateBuilder<*>.writeUpdate(request: Request) {
    this[RequestTable.isJoining] = request.isJoining
    this[RequestTable.comment] = request.comment
    this[RequestTable.requesterName] = request.requesterName
    this[RequestTable.createdAt] = request.createdAt.toLocalDateTimeUtc()
}