package streetlight.server.db.tables

import kabinet.model.UserId
import klutch.db.tables.UserTable
import klutch.utils.toStringId
import klutch.utils.toUUID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import streetlight.model.data.Song
import streetlight.model.data.SongId
import streetlight.server.utils.toProjectId
import streetlight.server.utils.toUserId

object SongTable : UUIDTable() {
    val userId = reference("user_id", UserTable, onDelete = ReferenceOption.CASCADE)
    val name = text("name")
    val artist = text("artist").nullable()
    val music = text("music").nullable()
}

fun ResultRow.toSong() = Song(
    songId = toProjectId(SongTable.id),
    userId = toUserId(SongTable.userId),
    name = this[SongTable.name],
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
    this[SongTable.name] = song.name
    this[SongTable.artist] = song.artist
    this[SongTable.music] = song.music
}