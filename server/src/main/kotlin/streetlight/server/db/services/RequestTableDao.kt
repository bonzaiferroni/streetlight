package streetlight.server.db.services

import klutch.db.DbService
import klutch.db.read
import klutch.utils.eq
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.insert
import streetlight.model.data.EventId
import streetlight.model.data.NewRequest
import streetlight.model.data.Request
import streetlight.model.data.RequestId
import streetlight.model.data.SongId
import streetlight.server.db.tables.RequestTable
import streetlight.server.db.tables.toRequest
import streetlight.server.db.tables.writeFull

class RequestTableDao: DbService() {

    suspend fun readRequest(requestId: RequestId) = dbQuery {
        RequestTable.read { it.id.eq(requestId) }.firstOrNull()?.toRequest()
    }

    suspend fun readRequests(eventId: EventId) = dbQuery {
        RequestTable.read { it.eventId.eq(eventId) }.map { it.toRequest() }
    }

    // Creates a request when the SongId is already known
    suspend fun createRequest(newRequest: NewRequest, songId: SongId): Request = dbQuery {
        val now = Clock.System.now()
        val request = Request(
            requestId = RequestId.random(),
            eventId = newRequest.eventId,
            songId = songId,
            isJoining = newRequest.isJoining,
            comment = newRequest.comment,
            requesterName = newRequest.requesterName,
            createdAt = now,
        )
        RequestTable.insert { it.writeFull(request) }
        request
    }

    // Convenience: attempts to create using the songId on NewRequest if present; returns null if missing
    suspend fun createRequest(newRequest: NewRequest): Request? = dbQuery {
        val sid = newRequest.songId ?: return@dbQuery null
        val now = Clock.System.now()
        val request = Request(
            requestId = RequestId.random(),
            eventId = newRequest.eventId,
            songId = sid,
            isJoining = newRequest.isJoining,
            comment = newRequest.comment,
            requesterName = newRequest.requesterName,
            createdAt = now,
        )
        RequestTable.insert { it.writeFull(request) }
        request
    }
}
