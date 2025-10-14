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
import org.jetbrains.exposed.sql.countDistinct
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
            console.log("playing request: ${song.title}")
            EventSong(
                song = song,
                request = nextRequest
            )
        } else {
            val leastPlayedSongId = SongTable.leftJoin(
                otherTable = RenditionTable,
                onColumn = { id },
                otherColumn = { songId },
                additionalConstraint = { RenditionTable.createdAt.greaterEq(since) }
            )
                .select(SongTable.id)
                .where { SongTable.userId.eq(userId) and SongTable.inRotation.eq(true) }
                .groupBy(SongTable.id)
                .orderBy(RenditionTable.id.count(), SortOrder.ASC_NULLS_FIRST)
                .limit(1)
                .firstOrNull()
                ?.let { it[SongTable.id].value }

            if (leastPlayedSongId == null) {
                console.logError("Unable to find song")
                null
            } else {
                val song = SongTable.readById(leastPlayedSongId).toSong()
                console.log("playing from catalog: ${song.title}")
                EventSong(request = null, song = song)
            }
        }
    }
}