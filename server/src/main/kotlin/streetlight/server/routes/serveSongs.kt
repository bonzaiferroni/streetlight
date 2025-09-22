package streetlight.server.routes

import io.ktor.server.routing.Routing
import klutch.server.*
import klutch.utils.getUserId
import streetlight.model.Api
import streetlight.server.db.services.SongTableDao

fun Routing.serveSongs(service: SongTableDao = SongTableDao()) {
    authenticateJwt {
        get(Api.Songs) {
            val userId = call.getUserId()
            service.readSongs(userId)
        }

        post(Api.Songs.Create) { newSong, endpoint ->
            val userId = call.getUserId()
            service.createSong(userId, newSong)
        }
    }
}