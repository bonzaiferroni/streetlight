package streetlight.server.db.services

import kabinet.model.UserId
import klutch.db.DbService
import klutch.db.deleteSingle
import klutch.db.read
import klutch.db.updateSingleWhere
import klutch.utils.eq
import klutch.utils.greaterEq
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insertAndGetId
import streetlight.model.data.SongId
import streetlight.model.data.Rendition
import streetlight.model.data.RenditionId
import streetlight.server.db.tables.RenditionTable
import streetlight.server.db.tables.toRendition
import streetlight.model.data.NewRendition
import streetlight.server.db.tables.writeFull
import streetlight.server.db.tables.writeUpdate
import streetlight.server.utils.toProjectId

class RenditionTableDao: DbService() {

    suspend fun readById(renditionId: RenditionId) = dbQuery {
        RenditionTable.read { it.id.eq(renditionId) }.firstOrNull()?.toRendition()
    }

    suspend fun readAllBySongId(songId: SongId) = dbQuery {
        RenditionTable.read { it.songId.eq(songId) }.map { it.toRendition() }
    }

    suspend fun readAllSince(userId: UserId, since: Instant) = dbQuery {
        RenditionTable.read {
            (it.userId.eq(userId)) and (it.createdAt.greaterEq(since))
        }.map { it.toRendition() }
    }

    suspend fun create(userId: UserId, songPlay: NewRendition): RenditionId = dbQuery {
        RenditionTable.insertAndGetId {
            it.writeFull(
                Rendition(
                    renditionId = RenditionId.random(),
                    songId = songPlay.songId,
                    userId = userId,
                    notes = songPlay.notes,
                    rating = songPlay.rating,
                    createdAt = Clock.System.now(),
                )
            )
        }.toProjectId()
    }

    suspend fun update(rendition: Rendition): Boolean = dbQuery {
        RenditionTable.updateSingleWhere({ RenditionTable.id.eq(rendition.renditionId) }) {
            it.writeUpdate(rendition)
        } != null
    }

    suspend fun delete(renditionId: RenditionId): Boolean = dbQuery {
        RenditionTable.deleteSingle { RenditionTable.id.eq(renditionId) }
    }
}
