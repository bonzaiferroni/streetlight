package streetlight.app.io

import pondui.io.NeoApiClient
import pondui.io.globalNeoApiClient
import streetlight.model.Api
import streetlight.model.data.NewSong

class SongApiClient(
    private val client: NeoApiClient = globalNeoApiClient
) {
    suspend fun readSongs() = client.request(Api.SongFeed)
    suspend fun createSong(newSong: NewSong) = client.request(Api.SongFeed.Create, newSong)
}