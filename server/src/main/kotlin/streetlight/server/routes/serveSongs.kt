package streetlight.server.routes

import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import klutch.server.*
import klutch.utils.getUserId
import streetlight.model.Api
import streetlight.server.db.services.LocationApiService
import streetlight.server.db.services.SongApiService

fun Routing.serveSongs(service: SongApiService = SongApiService()) {
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