package streetlight.app.io

import kabinet.api.write
import kotlinx.datetime.Instant
import pondui.io.NeoApiClient
import pondui.io.globalNeoApiClient
import streetlight.model.Api
import streetlight.model.data.NewSong

class SongApiClient(
    private val client: NeoApiClient = globalNeoApiClient
) {
    suspend fun readSongs() = client.request(Api.SongFeed)
    suspend fun createSong(newSong: NewSong) = client.request(Api.SongFeed.Create, newSong)
    suspend fun takeNextSong(since: Instant) = client.request(Api.SongFeed.TakeNextSong) {
        write(it.since, since)
    }
}