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

    get(Api.SongPlayFeed, { it.toProjectId() }) { id, _ ->
        dao.readSongPlay(id)
    }

    get(Api.SongPlayFeed.BySong, { it.toProjectId() }) { songId, _ ->
        dao.readSongPlays(songId)
    }

    authenticateJwt {
        post(Api.SongPlayFeed.Create) { newPlay, _ ->
            val userId = getUserId()
            dao.createSongPlay(userId, newPlay)
        }

        get(Api.SongPlayFeed.ReadAllSince) { endpoint ->
            val since: Instant = readParam(endpoint.since)
            val userId = getUserId()
            dao.readAllSince(userId, since)
        }

        post(Api.SongPlayFeed.Update) { play, _ ->
            dao.updateSongPlay(play)
        }

        delete(Api.SongPlayFeed.Delete) { songPlayId, _ ->
            dao.deleteSongPlay(songPlayId)
        }
    }
}
