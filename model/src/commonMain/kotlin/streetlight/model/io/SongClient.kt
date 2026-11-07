package streetlight.model.io

import kampfire.api.ApiTransport
import kampfire.api.getApi
import kampfire.api.postApi
import kampfire.model.Outcome
import streetlight.model.Api
import streetlight.model.data.*
import streetlight.model.music.*

/** The calls of `Api.Songs`. */
class SongClient(private val transport: ApiTransport) {
    suspend fun readSongs(): Outcome<List<SongLegacy>> = transport.getApi(Api.Songs)
    suspend fun createSong(song: NewSong): Outcome<SongId> = transport.postApi(Api.Songs.Create, song)
    suspend fun readSong(songId: SongId): Outcome<SongLegacy> = transport.getApi(Api.Songs.ReadId, songId)
    suspend fun updateSong(song: SongLegacy): Outcome<Boolean> = transport.postApi(Api.Songs.Update, song)
}
