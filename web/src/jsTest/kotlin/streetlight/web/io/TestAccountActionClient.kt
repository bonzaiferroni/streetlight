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

class TestAccountActionClient: AccountActionClient {
    override suspend fun verifyExistingEmail(): Outcome<Unit> = TODO()
    override suspend fun readEmailVerificationIsSent(): Outcome<Boolean> = TODO()
    override suspend fun removeEmail(password: PasswordVerification): Outcome<Unit> = TODO()
    override suspend fun resetPassword(email: EmailAddress): Outcome<Unit> = TODO()
    override suspend fun addEmail(value: EmailChange): Outcome<Unit> = TODO()
    override suspend fun changePassword(value: PasswordChange): Outcome<Unit> = TODO()
}
