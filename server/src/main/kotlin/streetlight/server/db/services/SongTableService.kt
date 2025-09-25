package streetlight.server.db.services

import kabinet.console.globalConsole
import kabinet.model.UserId
import klutch.db.DbService
import klutch.db.readById
import klutch.utils.eq
import klutch.utils.greaterEq
import klutch.utils.toUUID
import kotlinx.datetime.Instant
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.count
import streetlight.model.data.Song
import streetlight.model.data.SongId
import streetlight.server.RuntimeProvider
import streetlight.server.ServerProvider
import streetlight.server.db.tables.SongPlayTable
import streetlight.server.db.tables.SongTable
import streetlight.server.db.tables.toSong
import streetlight.server.utils.toProjectId

private val console = globalConsole.getHandle(SongTableService::class)

class SongTableService(val app: ServerProvider = RuntimeProvider): DbService() {

    suspend fun takeNextSong(userId: UserId, since: Instant): Song = dbQuery {
        val leastPlayedSongId = SongPlayTable.select(SongPlayTable.songId)
            .where { SongPlayTable.userId.eq(userId) and SongPlayTable.createdAt.greaterEq(since) }
            .groupBy(SongPlayTable.songId)
            .orderBy(SongPlayTable.songId.count())
            .limit(1)
            .firstOrNull()
            ?.let { it[SongPlayTable.songId].value }
            ?: SongTable.select(SongTable.id).first().let { it[SongTable.id].value }

        SongTable.readById(leastPlayedSongId).toSong()
    }
}