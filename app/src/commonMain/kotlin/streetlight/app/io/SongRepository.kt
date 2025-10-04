package streetlight.app.io

import kabinet.api.write
import kotlinx.datetime.Instant
import pondui.io.NeoApiClient
import pondui.io.globalNeoApiClient
import streetlight.model.Api
import streetlight.model.data.EventId
import streetlight.model.data.EventSong
import streetlight.model.data.NewSong
import streetlight.model.data.Song
import streetlight.model.data.SongId
import streetlight.model.mockDb

interface SongRepository {
    suspend fun readSongs(): List<Song>?
    suspend fun readById(songId: SongId): Song?
    suspend fun createSong(newSong: NewSong): SongId?
    suspend fun takeNextSong(eventId: EventId, since: Instant): EventSong?
    suspend fun updateSong(song: Song): Boolean
}

class SongApiClient(
    private val client: NeoApiClient = globalNeoApiClient
) : SongRepository {
    override suspend fun readSongs() = client.request(Api.SongFeed)
    override suspend fun readById(songId: SongId) = client.getById(Api.SongProfile, songId)
    override suspend fun createSong(newSong: NewSong) = client.request(Api.SongFeed.Create, newSong)
    override suspend fun takeNextSong(eventId: EventId, since: Instant) =
        client.getById(Api.SongFeed.TakeNextSong, eventId) {
            write(it.since, since)
        }

    override suspend fun updateSong(song: Song): Boolean = client.request(Api.SongProfile.Update, song) ?: false
}

class SongMockClient : SongRepository {
    override suspend fun readSongs(): List<Song>? = mockDb.songs
    override suspend fun readById(songId: SongId): Song? = mockDb.songs.firstOrNull { it.songId == songId }
    override suspend fun createSong(newSong: NewSong): SongId? = TODO("Not yet implemented")
    override suspend fun takeNextSong(eventId: EventId, since: Instant): EventSong = EventSong(
        song = mockDb.songs.first(),
        request = null,
    )

    override suspend fun updateSong(song: Song): Boolean {
        // In mock mode, assume update succeeds.
        return true
    }
}
