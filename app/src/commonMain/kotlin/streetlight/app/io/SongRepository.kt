package streetlight.app.io

import kabinet.api.write
import kotlinx.datetime.Instant
import pondui.io.NeoApiClient
import pondui.io.globalNeoApiClient
import streetlight.model.Api
import streetlight.model.data.NewSong
import streetlight.model.data.Song
import streetlight.model.data.SongId
import streetlight.model.mockDb

interface SongRepository {
    suspend fun readSongs(): List<Song>?
    suspend fun createSong(newSong: NewSong): SongId?
    suspend fun takeNextSong(since: Instant): Song?
}

class SongApiClient(
    private val client: NeoApiClient = globalNeoApiClient
): SongRepository {
    override suspend fun readSongs() = client.request(Api.SongFeed)
    override suspend fun createSong(newSong: NewSong) = client.request(Api.SongFeed.Create, newSong)
    override suspend fun takeNextSong(since: Instant) = client.request(Api.SongFeed.TakeNextSong) {
        write(it.since, since)
    }
}

class SongMockClient: SongRepository {
    override suspend fun readSongs(): List<Song>? = mockDb.songs
    override suspend fun createSong(newSong: NewSong): SongId? = TODO("Not yet implemented")
    override suspend fun takeNextSong(since: Instant): Song? = mockDb.songs.firstOrNull()
}
