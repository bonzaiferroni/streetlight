package streetlight.server.routes

import io.ktor.server.routing.Routing
import klutch.server.*
import klutch.utils.getUserId
import kotlinx.datetime.Instant
import streetlight.model.Api
import streetlight.server.RuntimeProvider
import streetlight.server.ServerProvider
import streetlight.model.data.toProjectId

fun Routing.serveSongs(app: ServerProvider = RuntimeProvider) {
    val dao = app.dao.song
    val service = app.service.song

    getEndpoint(Api.SongProfile, { it.toProjectId() }) { songId, _ ->
        dao.readById(songId)
    }

    authenticateJwt {
        getEndpoint(Api.SongFeed) {
            val userId = getUserId()
            dao.readAllByUserId(userId)
        }

        postEndpoint(Api.SongFeed.Create) { newSong, endpoint ->
            val userId = getUserId()
            dao.createSong(userId, newSong)
        }

        getEndpoint(Api.SongFeed.TakeNextSong, { it.toProjectId() }) { eventId, endpoint ->
            val since: Instant = readParam(endpoint.since)
            val userId = getUserId()
            service.takeNextSong(userId, eventId, since)
        }

        postEndpoint(Api.SongProfile.Update) { song, endpoint ->
            val userId = getUserId()
            dao.updateSong(userId, song)
        }
    }
}