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

class TestStarClient: StarClient {
    override suspend fun readPendingEdits(): Outcome<List<EditLog>> = TODO()
    override suspend fun readStarContent(username: Username): Outcome<StarContent> = TODO()
    override suspend fun readAccount(): Outcome<Account> = TODO()
    override suspend fun validateLogin(): Outcome<Star> = TODO()
    override suspend fun updateProfile(edit: StarEdit): Outcome<Star> = TODO()
    override suspend fun editStarLink(edit: EditLightRequest): Outcome<Boolean> = TODO()
    override suspend fun readProfileDesign(): Outcome<ProfileConfig> = TODO()
}
