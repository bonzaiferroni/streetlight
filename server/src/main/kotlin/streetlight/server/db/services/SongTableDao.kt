package streetlight.server.db.services

import kampfire.model.UserId
import klutch.db.DbService
import klutch.db.read
import klutch.utils.eq
import klutch.utils.toStringId
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.count
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.leftJoin
import org.jetbrains.exposed.sql.update
import streetlight.model.data.NewSong
import streetlight.model.data.RequestItem
import streetlight.model.data.Song
import streetlight.model.data.SongId
import streetlight.model.data.toProjectId
import streetlight.server.db.tables.RenditionTable
import streetlight.server.db.tables.SongTable
import streetlight.server.db.tables.toSong
import streetlight.server.db.tables.writeFull
import streetlight.server.db.tables.writeUpdate

class SongTableDao: DbService() {
    suspend fun readById(songId: SongId) = dbQuery {
        SongTable.read { it.id.eq(songId) }.firstOrNull()?.toSong()
    }

    suspend fun readAllByUserId(userId: UserId) = dbQuery {
        SongTable.read { it.userId.eq(userId) }
            .map { it.toSong() }
    }

    suspend fun createSong(userId: UserId, newSong: NewSong): SongId = dbQuery {
        val now = Clock.System.now()
        SongTable.insertAndGetId {
            it.writeFull(Song(
                songId = SongId.random(),
                userId = userId,
                title = newSong.title,
                artist = newSong.artist,
                tempo = null,
                capo = null,
                notation = null,
                inRotation = false,
                updatedAt = now,
                createdAt = now,
            ))
        }.value.toStringId().toProjectId()
    }

    suspend fun updateSong(userId: UserId, song: Song): Boolean = dbQuery {
        SongTable.update({ SongTable.userId.eq(userId) and SongTable.id.eq(song.songId) }) {
            it.writeUpdate(song)
        } > 0
    }

    suspend fun readRequestItems(userId: UserId) = dbQuery {
        SongTable.leftJoin(RenditionTable, { id }, { songId })
            .select(SongTable.columns + RenditionTable.songId.count())
            .where { SongTable.userId.eq(userId) }
            .groupBy(SongTable.id)
            .orderBy(RenditionTable.songId.count(), SortOrder.DESC_NULLS_LAST)
            .map {
                RequestItem(
                    song = it.toSong(),
                    plays = it.getOrNull(RenditionTable.songId.count())?.toInt() ?: 0
                )
            }
    }
}