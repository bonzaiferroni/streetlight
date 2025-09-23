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
    suspend fun readById(songPlayId: SongPlayId) = client.getById(Api.SongPlays, songPlayId)
    suspend fun readBySongId(songId: SongId) = client.getById(Api.SongPlays.Song, songId)
    suspend fun readAllSince(since: Instant) = client.request(Api.SongPlays.ReadAllSince) {
        write(it.since, since)
    }
    suspend fun create(newPlay: NewSongPlay) = client.request(Api.SongPlays.Create, newPlay)
    suspend fun update(play: SongPlay) = client.request(Api.SongPlays.Update, play)
    suspend fun delete(songPlayId: SongPlayId) = client.request(Api.SongPlays.Delete, songPlayId)
}
