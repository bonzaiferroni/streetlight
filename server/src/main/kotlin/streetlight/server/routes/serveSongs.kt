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

    get(Api.SongProfile, { it.toProjectId() }) { songId, _ ->
        dao.readById(songId)
    }

    authenticateJwt {
        get(Api.SongFeed) {
            val userId = getUserId()
            dao.readAllByUserId(userId)
        }

        post(Api.SongFeed.Create) { newSong, endpoint ->
            val userId = getUserId()
            dao.createSong(userId, newSong)
        }

        get(Api.SongFeed.TakeNextSong) { endpoint ->
            val since: Instant = readParam(endpoint.since)
            val userId = getUserId()
            service.takeNextSong(userId, since)
        }

        post(Api.SongProfile.Update) { song, endpoint ->
            val userId = getUserId()
            dao.updateSong(userId, song)
        }
    }
}