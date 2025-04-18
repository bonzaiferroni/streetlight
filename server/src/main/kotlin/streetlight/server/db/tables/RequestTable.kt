package streetlight.server.db.tables

import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.ResultRow
import streetlight.model.core.Request

internal object RequestTable : IntIdTable() {
    val eventId = reference("event_id", EventTable, onDelete = ReferenceOption.CASCADE)
    val songId = reference("song_id", SongTable, onDelete = ReferenceOption.CASCADE)
    val time = long("time")
    val performed = bool("performed")
    val notes = text("notes")
    val requesterName = text("requester_name").nullable()
}

internal fun ResultRow.toRequest() = Request(
    id = this[RequestTable.id].value,
    eventId = this[RequestTable.eventId].value,
    songId = this[RequestTable.songId].value,
    time = this[RequestTable.time],
    performed = this[RequestTable.performed],
    notes = this[RequestTable.notes],
    requesterName = this[RequestTable.requesterName]
)