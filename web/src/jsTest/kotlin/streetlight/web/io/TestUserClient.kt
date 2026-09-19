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

class TestUserClient: UserClient {
    override suspend fun createUser(request: SignUpRequest): Outcome<Unit> = TODO()
    override suspend fun login(request: LoginRequest): Outcome<Unit> = TODO()
    override suspend fun checkGuest(): Outcome<Username?> = TODO()
    override suspend fun upgradeAccount(request: AccountUpgradeRequest): Outcome<Boolean> = TODO()
    override suspend fun logout(): Outcome<Boolean> = TODO()
    override suspend fun checkUsernameExists(username: Username): Outcome<Boolean> = TODO()
    override suspend fun generateUsername(): Outcome<Username> = TODO()
    override suspend fun uploadImageBlob(blobImage: Image): Outcome<Image> = TODO()
    override suspend fun readTalents(): Outcome<List<Talent>> = TODO()
    override suspend fun editTalent(talent: TalentEdit): Outcome<Talent> = TODO()
}
