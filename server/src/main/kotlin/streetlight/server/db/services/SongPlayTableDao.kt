package streetlight.server.db.services

import kabinet.model.UserId
import klutch.db.DbService
import klutch.db.deleteSingle
import klutch.db.read
import klutch.db.updateSingleWhere
import klutch.utils.eq
import klutch.utils.greaterEq
import klutch.utils.toStringId
import klutch.utils.toUUID
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insertAndGetId
import streetlight.model.data.SelfRating
import streetlight.model.data.SongId
import streetlight.model.data.SongPlay
import streetlight.model.data.SongPlayId
import streetlight.model.data.toProjectId
import streetlight.server.db.tables.SongPlayTable
import streetlight.server.db.tables.toSongPlay
import streetlight.model.data.NewSongPlay
import streetlight.server.db.tables.writeFull
import streetlight.server.db.tables.writeUpdate
import streetlight.server.utils.toProjectId

class SongPlayTableDao: DbService() {

    suspend fun readSongPlay(songPlayId: SongPlayId) = dbQuery {
        SongPlayTable.read { it.id.eq(songPlayId) }.firstOrNull()?.toSongPlay()
    }

    suspend fun readSongPlays(songId: SongId) = dbQuery {
        SongPlayTable.read { it.songId.eq(songId) }.map { it.toSongPlay() }
    }

    suspend fun readAllSince(userId: UserId, since: Instant) = dbQuery {
        SongPlayTable.read {
            (it.userId.eq(userId)) and (it.createdAt.greaterEq(since))
        }.map { it.toSongPlay() }
    }

    suspend fun createSongPlay(userId: UserId, songPlay: NewSongPlay): SongPlayId = dbQuery {
        SongPlayTable.insertAndGetId {
            it.writeFull(
                SongPlay(
                    songPlayId = SongPlayId.random(),
                    songId = songPlay.songId,
                    userId = userId,
                    notes = songPlay.notes,
                    rating = songPlay.rating,
                    createdAt = Clock.System.now(),
                )
            )
        }.toProjectId()
    }

    suspend fun updateSongPlay(songPlay: SongPlay): Boolean = dbQuery {
        SongPlayTable.updateSingleWhere({ SongPlayTable.id.eq(songPlay.songPlayId) }) {
            it.writeUpdate(songPlay)
        } != null
    }

    suspend fun deleteSongPlay(songPlayId: SongPlayId): Boolean = dbQuery {
        SongPlayTable.deleteSingle { SongPlayTable.id.eq(songPlayId) }
    }
}
