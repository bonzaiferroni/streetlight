package streetlight.app.io

import pondui.io.NeoApiClient
import pondui.io.globalNeoApiClient
import streetlight.model.Api
import streetlight.model.data.NewSong

class SongStore(
    private val client: NeoApiClient = globalNeoApiClient
) {
    suspend fun readSongs() = client.request(Api.Songs)
    suspend fun createSong(newSong: NewSong) = client.request(Api.Songs.Create, newSong)
}