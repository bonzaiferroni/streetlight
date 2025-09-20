package streetlight.server.db.tables

import kabinet.model.UserId
import klutch.db.tables.UserTable
import klutch.utils.toStringId
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.ResultRow
import streetlight.model.data.Song
import streetlight.model.data.SongId

internal object SongTable : UUIDTable() {
    val userId = reference("user_id", UserTable, onDelete = ReferenceOption.CASCADE)
    val name = text("name")
    val artist = text("artist").nullable()
    val music = text("music").nullable()
}

internal fun ResultRow.toSong() = Song(
    songId = SongId(this[SongTable.id].value.toStringId()),
    userId = UserId(this[SongTable.userId].value.toStringId()),
    name = this[SongTable.name],
    artist = this[SongTable.artist],
    music = this[SongTable.music],
)