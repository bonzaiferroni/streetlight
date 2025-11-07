package streetlight.server.db.tables

import kabinet.utils.toInstantFromUtc
import kabinet.utils.toLocalDateTimeUtc
import klutch.db.tables.UserTable
import klutch.utils.toUUID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.json.jsonb
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import streetlight.model.data.Performer
import streetlight.server.utils.toProjectId
import streetlight.server.utils.toUserIdOrNull

object PerformerTable : UUIDTable("performer") {
    val userId = reference("user_id", UserTable, onDelete = ReferenceOption.SET_NULL).nullable()
    val name = text("name").nullable()
    val songs = jsonb<List<String>>("songs", tableJsonDefault).nullable()
    val createdAt = datetime("created_at")
}

fun ResultRow.toPerformer() = Performer(
    performerId = toProjectId(PerformerTable.id),
    userId = toUserIdOrNull(PerformerTable.userId),
    name = this[PerformerTable.name],
    songs = this[PerformerTable.songs],
    createdAt = this[PerformerTable.createdAt].toInstantFromUtc(),
)

fun UpdateBuilder<*>.writeFull(performer: Performer) {
    this[PerformerTable.id] = performer.performerId.toUUID()
    this[PerformerTable.userId] = performer.userId?.toUUID()
    this[PerformerTable.createdAt] = performer.createdAt.toLocalDateTimeUtc()
    writeUpdate(performer)
}

fun UpdateBuilder<*>.writeUpdate(performer: Performer) {
    this[PerformerTable.name] = performer.name
    this[PerformerTable.songs] = performer.songs
}
