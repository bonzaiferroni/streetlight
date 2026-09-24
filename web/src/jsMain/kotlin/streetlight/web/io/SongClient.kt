package streetlight.web.io

import kampfire.api.EmailAddress
import kampfire.api.Slug
import kampfire.api.UserApi
import kampfire.api.Username
import kampfire.model.AccountUpgradeRequest
import kampfire.model.EmailChange
import kampfire.model.GeoPoint
import kampfire.model.GeoRect
import kampfire.model.LoginRequest
import kampfire.model.Outcome
import kampfire.model.PasswordChange
import kampfire.model.PasswordVerification
import kampfire.model.SignUpRequest
import kampfire.model.Url
import koala.Image
import koala.model.DocId
import koala.model.DocTableItem
import kotlinx.coroutines.CoroutineScope
import streetlight.model.Api
import streetlight.model.data.*
import streetlight.model.writeCursor
import web.sockets.WebSocket
import web.sse.EventSource
import kotlin.uuid.Uuid

/** The calls of `Api.Songs`. */
interface SongClient {
    suspend fun readSongs(): Outcome<List<Song>>
    suspend fun createSong(song: NewSong): Outcome<SongId>
    suspend fun readSong(songId: SongId): Outcome<Song>
    suspend fun updateSong(song: Song): Outcome<Boolean>
}

class BrowserSongClient(private val client: FetchClient): SongClient {
    override suspend fun readSongs() = client.getApi(Api.Songs)
    override suspend fun createSong(song: NewSong) = client.postApi(Api.Songs.Create, song)
    override suspend fun readSong(songId: SongId) = client.getApi(Api.Songs.ReadId, songId)
    override suspend fun updateSong(song: Song) = client.postApi(Api.Songs.Update, song)
}
