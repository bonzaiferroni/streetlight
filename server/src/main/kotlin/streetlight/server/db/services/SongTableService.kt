package streetlight.server.db.services

import kabinet.console.globalConsole
import kabinet.model.UserId
import klutch.db.DbService
import klutch.db.readById
import klutch.utils.eq
import klutch.utils.greaterEq
import kotlinx.datetime.Instant
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.count
import streetlight.model.data.Song
import streetlight.server.RuntimeProvider
import streetlight.server.ServerProvider
import streetlight.server.db.tables.RenditionTable
import streetlight.server.db.tables.SongTable
import streetlight.server.db.tables.toSong

private val console = globalConsole.getHandle(SongTableService::class)

class SongTableService(val app: ServerProvider = RuntimeProvider): DbService() {

    suspend fun takeNextSong(userId: UserId, since: Instant): Song = dbQuery {
        val leastPlayedSongId = RenditionTable.select(RenditionTable.songId)
            .where { RenditionTable.userId.eq(userId) and RenditionTable.createdAt.greaterEq(since) }
            .groupBy(RenditionTable.songId)
            .orderBy(RenditionTable.songId.count())
            .limit(1)
            .firstOrNull()
            ?.let { it[RenditionTable.songId].value }
            ?: SongTable.select(SongTable.id).first().let { it[SongTable.id].value }

        SongTable.readById(leastPlayedSongId).toSong()
    }
}