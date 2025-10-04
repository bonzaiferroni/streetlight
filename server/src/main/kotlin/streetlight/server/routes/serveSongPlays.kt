package streetlight.server.routes

import io.ktor.server.routing.Routing
import klutch.server.*
import klutch.utils.getUserId
import kotlinx.datetime.Instant
import streetlight.model.Api
import streetlight.model.data.toProjectId
import streetlight.server.RuntimeProvider
import streetlight.server.ServerProvider

fun Routing.serveSongPlays(app: ServerProvider = RuntimeProvider) {
    val dao = app.dao.songPlay

    getEndpoint(Api.RenditionFeed, { it.toProjectId() }) { id, _ ->
        dao.readSongPlay(id)
    }

    getEndpoint(Api.RenditionFeed.BySong, { it.toProjectId() }) { songId, _ ->
        dao.readSongPlays(songId)
    }

    authenticateJwt {
        postEndpoint(Api.RenditionFeed.Create) { newPlay, _ ->
            val userId = getUserId()
            dao.createSongPlay(userId, newPlay)
        }

        getEndpoint(Api.RenditionFeed.ReadAllSince) { endpoint ->
            val since: Instant = readParam(endpoint.since)
            val userId = getUserId()
            dao.readAllSince(userId, since)
        }

        postEndpoint(Api.RenditionFeed.Update) { play, _ ->
            dao.updateSongPlay(play)
        }

        deleteEndpoint(Api.RenditionFeed.Delete) { songPlayId, _ ->
            dao.deleteSongPlay(songPlayId)
        }
    }
}
