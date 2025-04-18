package streetlight.server.db.tables

import klutch.db.tables.UserTable
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.ResultRow
import streetlight.model.core.Song

internal object SongTable : IntIdTable() {
    val userId = reference("user_id", UserTable, onDelete = ReferenceOption.CASCADE)
    val name = text("name")
    val artist = text("artist").nullable()
    val music = text("music").nullable()
}

internal fun ResultRow.toSong() = Song(
    id = this[SongTable.id].value,
    userId = this[SongTable.userId].value,
    name = this[SongTable.name],
    artist = this[SongTable.artist],
    music = this[SongTable.music]
)