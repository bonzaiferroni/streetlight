package streetlight.server.db.tables

import kabinet.utils.toInstantFromUtc
import klutch.utils.toStringId
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import streetlight.model.data.EventId
import streetlight.model.data.Request
import streetlight.model.data.RequestId
import streetlight.model.data.SongId

internal object RequestTable : UUIDTable() {
    val eventId = reference("event_id", EventTable, onDelete = ReferenceOption.CASCADE)
    val songId = reference("song_id", SongTable, onDelete = ReferenceOption.CASCADE)
    val performed = bool("performed")
    val notes = text("notes")
    val requesterName = text("requester_name").nullable()
    val requestedAt = datetime("requested_at")
}

internal fun ResultRow.toRequest() = Request(
    requestId = RequestId(this[RequestTable.id].value.toStringId()),
    eventId = EventId(this[RequestTable.eventId].value.toStringId()),
    songId = SongId(this[RequestTable.songId].value.toStringId()),
    performed = this[RequestTable.performed],
    notes = this[RequestTable.notes],
    requesterName = this[RequestTable.requesterName],
    requestedAt = this[RequestTable.requestedAt].toInstantFromUtc(),
)