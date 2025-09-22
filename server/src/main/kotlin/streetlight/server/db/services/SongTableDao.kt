package streetlight.server.db.services

import kabinet.model.UserId
import klutch.db.DbService
import klutch.db.read
import klutch.utils.eq
import klutch.utils.toStringId
import klutch.utils.toUUID
import org.jetbrains.exposed.sql.insertAndGetId
import streetlight.model.data.NewSong
import streetlight.model.data.SongId
import streetlight.model.data.toProjectId
import streetlight.server.db.tables.SongTable
import streetlight.server.db.tables.toSong

class SongTableDao: DbService() {
    suspend fun readSongs(userId: UserId) = dbQuery {
        SongTable.read { it.userId.eq(userId) }
            .map { it.toSong() }
    }

    suspend fun createSong(userId: UserId, newSong: NewSong): SongId = dbQuery {
        SongTable.insertAndGetId {
            it[this.userId] = userId.toUUID()
            it[this.name] = newSong.name
            it[this.artist] = newSong.artist
        }.value.toStringId().toProjectId()
    }
}