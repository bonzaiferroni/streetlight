package streetlight.server.db.services

import klutch.db.DbService
import klutch.db.read
import org.jetbrains.exposed.sql.insertAndGetId
import streetlight.model.data.NewSong
import streetlight.server.db.tables.SongTable
import streetlight.server.db.tables.toSong

class SongApiService: DbService() {
    suspend fun readSongs(userId: Long) = dbQuery {
        SongTable.read { it.userId.eq(userId) }
            .map { it.toSong() }
    }

    suspend fun createSong(userId: Long, newSong: NewSong) = dbQuery {
        SongTable.insertAndGetId {
            it[this.userId] = userId
            it[this.name] = newSong.name
            it[this.artist] = newSong.artist
        }.value
    }
}