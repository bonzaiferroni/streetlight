package streetlight.model.io

import kampfire.api.ApiTransport
import kampfire.api.EmailAddress
import kampfire.api.Slug
import kampfire.api.UserApi
import kampfire.api.Username
import kampfire.api.getApi
import kampfire.api.postApi
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
import kotlin.uuid.Uuid
import streetlight.model.Api
import streetlight.model.data.*

/** The calls of `Api.Songs`. */
class SongClient(private val transport: ApiTransport) {
    suspend fun readSongs(): Outcome<List<Song>> = transport.getApi(Api.Songs)
    suspend fun createSong(song: NewSong): Outcome<SongId> = transport.postApi(Api.Songs.Create, song)
    suspend fun readSong(songId: SongId): Outcome<Song> = transport.getApi(Api.Songs.ReadId, songId)
    suspend fun updateSong(song: Song): Outcome<Boolean> = transport.postApi(Api.Songs.Update, song)
}
