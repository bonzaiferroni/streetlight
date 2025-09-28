package streetlight.server.db.tables

import kabinet.utils.toInstantFromUtc
import kabinet.utils.toLocalDateTimeUtc
import klutch.db.tables.UserTable
import klutch.utils.toUUID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import streetlight.model.data.SelfRating
import streetlight.model.data.Rendition
import streetlight.server.utils.toProjectId
import streetlight.server.utils.toUserId

object RenditionTable : UUIDTable() {
    val songId = reference("song_id", SongTable, onDelete = ReferenceOption.CASCADE)
    val userId = reference("user_Id", UserTable, onDelete = ReferenceOption.CASCADE)
    val notes = text("notes").nullable()
    val rating = enumeration<SelfRating>("rating").nullable()
    val createdAt = datetime("created_at")
}

fun ResultRow.toRendition() = Rendition(
    renditionId = toProjectId(RenditionTable.id),
    songId = toProjectId(RenditionTable.songId),
    userId = toUserId(RenditionTable.userId),
    notes = this[RenditionTable.notes],
    rating = this[RenditionTable.rating],
    createdAt = this[RenditionTable.createdAt].toInstantFromUtc(),
)

// Updaters
fun UpdateBuilder<*>.writeFull(rendition: Rendition) {
    this[RenditionTable.id] = rendition.renditionId.toUUID()
    this[RenditionTable.songId] = rendition.songId.toUUID()
    this[RenditionTable.userId] = rendition.userId.toUUID()
    writeUpdate(rendition)
}

fun UpdateBuilder<*>.writeUpdate(rendition: Rendition) {
    this[RenditionTable.notes] = rendition.notes
    this[RenditionTable.rating] = rendition.rating
    this[RenditionTable.createdAt] = rendition.createdAt.toLocalDateTimeUtc()
}