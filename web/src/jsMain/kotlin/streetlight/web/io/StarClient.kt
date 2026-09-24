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

/** The calls of `Api.Stars`, for the signed-in user and user pages. */
interface StarClient {
    suspend fun readPendingEdits(): Outcome<List<EditLog>>
    suspend fun readStarContent(username: Username): Outcome<StarContent>
    suspend fun readAccount(): Outcome<Account>
    suspend fun validateLogin(): Outcome<Star>
    suspend fun updateProfile(edit: StarEdit): Outcome<Star>
    suspend fun editStarLink(edit: EditLightRequest): Outcome<Boolean>
    suspend fun readProfileDesign(): Outcome<ProfileConfig>
}

class BrowserStarClient(private val client: FetchClient): StarClient {
    override suspend fun readPendingEdits() = client.getApi(Api.Stars.PendingEdits)
    override suspend fun readStarContent(username: Username) =
        client.getApi(Api.Stars.ReadStarContent) {
            writeParam(it.username, username)
        }
    override suspend fun readAccount() = client.getApi(Api.Stars.ReadAccount)
    override suspend fun validateLogin() = client.getApi(Api.Stars.ValidateLogin)
    override suspend fun updateProfile(edit: StarEdit) = client.postApi(Api.Stars.UpdateProfile, edit)
    override suspend fun editStarLink(edit: EditLightRequest) = client.postApi(Api.Stars.EditLight, edit)
    override suspend fun readProfileDesign() = client.getApi(Api.Stars.ReadProfileConfig)
}
