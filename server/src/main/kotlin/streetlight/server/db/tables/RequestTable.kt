package streetlight.server.db.tables

import klutch.utils.toInstantUtc
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import streetlight.model.data.Request

internal object RequestTable : LongIdTable() {
    val eventId = reference("event_id", EventTable, onDelete = ReferenceOption.CASCADE)
    val songId = reference("song_id", SongTable, onDelete = ReferenceOption.CASCADE)
    val performed = bool("performed")
    val notes = text("notes")
    val requesterName = text("requester_name").nullable()
    val requestedAt = datetime("requested_at")
}

internal fun ResultRow.toRequest() = Request(
    id = this[RequestTable.id].value,
    eventId = this[RequestTable.eventId].value,
    songId = this[RequestTable.songId].value,
    performed = this[RequestTable.performed],
    notes = this[RequestTable.notes],
    requesterName = this[RequestTable.requesterName],
    requestedAt = this[RequestTable.requestedAt].toInstantUtc(),
)