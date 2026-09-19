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

interface MediaClient {
    suspend fun readMedia(slug: Slug): Outcome<Media>
    suspend fun createMedia(edit: MediaEdit): Outcome<Media>
    suspend fun updateMedia(edit: MediaEdit): Outcome<Media>
}

class BrowserMediaClient(private val client: FetchClient): MediaClient {
    override suspend fun readMedia(slug: Slug) = client.getApi(Api.Medias.ReadMedia, slug)
    override suspend fun createMedia(edit: MediaEdit) = client.postApi(Api.Medias.CreateMedia, edit)
    override suspend fun updateMedia(edit: MediaEdit) = client.postApi(Api.Medias.UpdateMedia, edit)
}
