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

/** The calls of `UserApi` and `Api.Users`: signing in and out, signing up, and uploads. */
interface UserClient {
    suspend fun createUser(request: SignUpRequest): Outcome<Unit>
    suspend fun login(request: LoginRequest): Outcome<Unit>
    suspend fun checkGuest(): Outcome<Username?>
    suspend fun upgradeAccount(request: AccountUpgradeRequest): Outcome<Boolean>
    suspend fun logout(): Outcome<Boolean>
    suspend fun checkUsernameExists(username: Username): Outcome<Boolean>
    suspend fun generateUsername(): Outcome<Username>
    suspend fun uploadImageBlob(blobImage: Image): Outcome<Image>
    suspend fun readTalents(): Outcome<List<Talent>>
    suspend fun editTalent(talent: TalentEdit): Outcome<Talent>
}

class BrowserUserClient(private val client: FetchClient): UserClient {
    override suspend fun createUser(request: SignUpRequest) = client.postApi(UserApi.Create, request)
    override suspend fun login(request: LoginRequest) = client.postApi(UserApi.Login, request)
    override suspend fun checkGuest() = client.getApi(UserApi.Login.CheckGuest)
    override suspend fun upgradeAccount(request: AccountUpgradeRequest) = client.postApi(UserApi.AccountUpgrade, request)
    override suspend fun logout() = client.postApi(UserApi.Logout, Unit)
    override suspend fun checkUsernameExists(username: Username) = client.postApi(UserApi.CheckUsernameExists, username)
    override suspend fun generateUsername() = client.getApi(UserApi.GenerateUsername)
    override suspend fun uploadImageBlob(blobImage: Image) = client.uploadBlob(Api.Users.UploadImage.path, blobImage)
    override suspend fun readTalents() = client.getApi(Api.Users.Talents)
    override suspend fun editTalent(talent: TalentEdit) = client.postApi(Api.Users.EditTalent, talent)
}
