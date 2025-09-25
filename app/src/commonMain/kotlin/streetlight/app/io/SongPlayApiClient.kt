package streetlight.app.io

import kabinet.api.write
import pondui.io.NeoApiClient
import pondui.io.globalNeoApiClient
import streetlight.model.Api
import streetlight.model.data.NewSongPlay
import streetlight.model.data.SongPlay
import streetlight.model.data.SongId
import streetlight.model.data.SongPlayId
import kotlinx.datetime.Instant

class SongPlayApiClient(
    private val client: NeoApiClient = globalNeoApiClient
) {
    suspend fun readById(songPlayId: SongPlayId) = client.getById(Api.SongPlayFeed, songPlayId)
    suspend fun readBySongId(songId: SongId) = client.getById(Api.SongPlayFeed.BySong, songId)
    suspend fun readAllSince(since: Instant) = client.request(Api.SongPlayFeed.ReadAllSince) {
        write(it.since, since)
    }
    suspend fun create(newPlay: NewSongPlay) = client.request(Api.SongPlayFeed.Create, newPlay)
    suspend fun update(play: SongPlay) = client.request(Api.SongPlayFeed.Update, play)
    suspend fun delete(songPlayId: SongPlayId) = client.request(Api.SongPlayFeed.Delete, songPlayId)
}
