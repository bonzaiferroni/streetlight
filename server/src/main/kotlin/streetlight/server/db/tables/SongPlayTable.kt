package streetlight.server.db.tables

import kabinet.utils.toInstantFromUtc
import kabinet.utils.toLocalDateTimeUtc
import klutch.db.tables.UserTable
import klutch.utils.toPGpoint
import klutch.utils.toStringId
import klutch.utils.toUUID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import streetlight.model.data.Location
import streetlight.model.data.SelfRating
import streetlight.model.data.SongId
import streetlight.model.data.SongPlay
import streetlight.model.data.SongPlayId
import streetlight.server.utils.toProjectId
import streetlight.server.utils.toUserId

object SongPlayTable : UUIDTable() {
    val songId = reference("song_id", SongTable, onDelete = ReferenceOption.CASCADE)
    val userId = reference("user_Id", UserTable, onDelete = ReferenceOption.CASCADE)
    val notes = text("notes").nullable()
    val rating = enumeration<SelfRating>("rating").nullable()
    val createdAt = datetime("created_at")
}

fun ResultRow.toSongPlay() = SongPlay(
    songPlayId = toProjectId(SongPlayTable.id),
    songId = toProjectId(SongPlayTable.songId),
    userId = toUserId(SongPlayTable.userId),
    notes = this[SongPlayTable.notes],
    rating = this[SongPlayTable.rating],
    createdAt = this[SongPlayTable.createdAt].toInstantFromUtc(),
)

// Updaters
fun UpdateBuilder<*>.writeFull(songPlay: SongPlay) {
    this[SongPlayTable.id] = songPlay.songPlayId.toUUID()
    this[SongPlayTable.songId] = songPlay.songId.toUUID()
    this[SongPlayTable.userId] = songPlay.userId.toUUID()
    writeUpdate(songPlay)
}

fun UpdateBuilder<*>.writeUpdate(songPlay: SongPlay) {
    this[SongPlayTable.notes] = songPlay.notes
    this[SongPlayTable.rating] = songPlay.rating
    this[SongPlayTable.createdAt] = songPlay.createdAt.toLocalDateTimeUtc()
}