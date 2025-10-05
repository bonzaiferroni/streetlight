package streetlight.server.db.services

import kabinet.console.globalConsole
import kabinet.model.UserId
import klutch.db.DbService
import klutch.db.deleteSingle
import klutch.db.readById
import klutch.db.readFirstOrNull
import klutch.utils.eq
import klutch.utils.greaterEq
import klutch.utils.toUUID
import kotlinx.datetime.Instant
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.alias
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.count
import org.jetbrains.exposed.sql.leftJoin
import org.jetbrains.exposed.sql.longLiteral
import streetlight.model.data.EventId
import streetlight.model.data.EventSong
import streetlight.model.data.Song
import streetlight.server.RuntimeProvider
import streetlight.server.ServerProvider
import streetlight.server.db.tables.EventTable
import streetlight.server.db.tables.RenditionTable
import streetlight.server.db.tables.RequestTable
import streetlight.server.db.tables.SongTable
import streetlight.server.db.tables.toRequest
import streetlight.server.db.tables.toSong

private val console = globalConsole.getHandle(SongTableService::class)

class SongTableService(val app: ServerProvider = RuntimeProvider): DbService() {

    suspend fun takeNextSong(userId: UserId, eventId: EventId, since: Instant) = dbQuery {
        val nextRequest = EventTable.innerJoin(RequestTable).select(RequestTable.columns)
            .where { EventTable.userId.eq(userId) and RequestTable.eventId.eq(eventId) }
            .orderBy(RequestTable.createdAt)
            .limit(1)
            .firstOrNull()?.toRequest()

        if (nextRequest != null) {
            RequestTable.deleteSingle { it.id.eq(nextRequest.requestId) }
            val song = SongTable.readById(nextRequest.songId.toUUID()).toSong()
            EventSong(
                song = song,
                request = nextRequest
            )
        } else {
            val leastPlayedSongId = RenditionTable.select(RenditionTable.songId)
                .where { RenditionTable.userId.eq(userId) and RenditionTable.createdAt.greaterEq(since) }
                .groupBy(RenditionTable.songId)
                .orderBy(RenditionTable.songId.count())
                .limit(1)
                .firstOrNull()
                ?.let { it[RenditionTable.songId].value }
                ?: SongTable.select(SongTable.id).first().let { it[SongTable.id].value }

            val song = SongTable.readById(leastPlayedSongId).toSong()
            EventSong(request = null, song = song)
        }
    }
}