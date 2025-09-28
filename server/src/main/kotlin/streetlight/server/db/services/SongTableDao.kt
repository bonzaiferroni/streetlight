package streetlight.server.db.services

import kabinet.model.UserId
import klutch.db.DbService
import klutch.db.read
import klutch.utils.eq
import klutch.utils.toStringId
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.insertAndGetId
import streetlight.model.data.NewSong
import streetlight.model.data.Song
import streetlight.model.data.SongId
import streetlight.model.data.toProjectId
import streetlight.server.db.tables.SongTable
import streetlight.server.db.tables.toSong
import streetlight.server.db.tables.writeFull

class SongTableDao: DbService() {
    suspend fun readSongs(userId: UserId) = dbQuery {
        SongTable.read { it.userId.eq(userId) }
            .map { it.toSong() }
    }

    suspend fun createSong(userId: UserId, newSong: NewSong): SongId = dbQuery {
        SongTable.insertAndGetId {
            it.writeFull(Song(
                songId = SongId.random(),
                userId = userId,
                title = newSong.title,
                artist = newSong.artist,
                tempo = null,
                capo = null,
                notation = null,
                createdAt = Clock.System.now(),
            ))
        }.value.toStringId().toProjectId()
    }
}