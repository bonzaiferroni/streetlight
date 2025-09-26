package streetlight.app.io

import kabinet.api.write
import kotlinx.datetime.Instant
import pondui.io.NeoApiClient
import pondui.io.globalNeoApiClient
import streetlight.model.Api
import streetlight.model.data.NewSongPlay
import streetlight.model.data.SongPlay
import streetlight.model.data.SongId
import streetlight.model.data.SongPlayId
import streetlight.model.mockDb

interface SongPlayRepository {
    suspend fun readById(songPlayId: SongPlayId): SongPlay?
    suspend fun readBySongId(songId: SongId): List<SongPlay>?
    suspend fun readAllSince(since: Instant): List<SongPlay>?
    suspend fun create(newPlay: NewSongPlay): SongPlayId?
    suspend fun update(play: SongPlay): Boolean?
    suspend fun delete(songPlayId: SongPlayId): Boolean?
}

class SongPlayApiClient(
    private val client: NeoApiClient = globalNeoApiClient
): SongPlayRepository {
    override suspend fun readById(songPlayId: SongPlayId) = client.getById(Api.SongPlayFeed, songPlayId)
    override suspend fun readBySongId(songId: SongId) = client.getById(Api.SongPlayFeed.BySong, songId)
    override suspend fun readAllSince(since: Instant) = client.request(Api.SongPlayFeed.ReadAllSince) {
        write(it.since, since)
    }
    override suspend fun create(newPlay: NewSongPlay) = client.request(Api.SongPlayFeed.Create, newPlay)
    override suspend fun update(play: SongPlay) = client.request(Api.SongPlayFeed.Update, play)
    override suspend fun delete(songPlayId: SongPlayId) = client.request(Api.SongPlayFeed.Delete, songPlayId)
}

class SongPlayMockClient: SongPlayRepository {
    override suspend fun readById(songPlayId: SongPlayId): SongPlay? = null
    override suspend fun readBySongId(songId: SongId): List<SongPlay>? = emptyList()
    override suspend fun readAllSince(since: Instant): List<SongPlay>? = emptyList()
    override suspend fun create(newPlay: NewSongPlay): SongPlayId? = TODO("Not yet implemented")
    override suspend fun update(play: SongPlay): Boolean? = TODO("Not yet implemented")
    override suspend fun delete(songPlayId: SongPlayId): Boolean? = TODO("Not yet implemented")
}
