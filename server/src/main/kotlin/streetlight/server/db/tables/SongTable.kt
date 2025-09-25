package streetlight.server.db.tables

import klutch.db.tables.UserTable
import klutch.utils.toUUID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import streetlight.model.data.Song
import streetlight.server.utils.toProjectId
import streetlight.server.utils.toUserId

object SongTable : UUIDTable() {
    val userId = reference("user_id", UserTable, onDelete = ReferenceOption.CASCADE)
    val name = text("title")
    val artist = text("artist").nullable()
    val music = text("music").nullable()
}

fun ResultRow.toSong() = Song(
    songId = toProjectId(SongTable.id),
    userId = toUserId(SongTable.userId),
    title = this[SongTable.name],
    artist = this[SongTable.artist],
    music = this[SongTable.music],
)

// Updaters
fun UpdateBuilder<*>.writeFull(song: Song) {
    this[SongTable.id] = song.songId.toUUID()
    this[SongTable.userId] = song.userId.toUUID()
    writeUpdate(song)
}

fun UpdateBuilder<*>.writeUpdate(song: Song) {
    this[SongTable.name] = song.title
    this[SongTable.artist] = song.artist
    this[SongTable.music] = song.music
}