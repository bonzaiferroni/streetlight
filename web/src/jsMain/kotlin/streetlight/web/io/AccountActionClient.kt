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

interface AccountActionClient {
    suspend fun verifyExistingEmail(): Outcome<Unit>
    suspend fun readEmailVerificationIsSent(): Outcome<Boolean>
    suspend fun removeEmail(password: PasswordVerification): Outcome<Unit>
    suspend fun resetPassword(email: EmailAddress): Outcome<Unit>
    suspend fun addEmail(value: EmailChange): Outcome<Unit>
    suspend fun changePassword(value: PasswordChange): Outcome<Unit>
}

class BrowserAccountActionClient(private val client: FetchClient): AccountActionClient {
    override suspend fun verifyExistingEmail() = client.postApi(Api.AccountAction.VerifyExistingEmail)
    override suspend fun readEmailVerificationIsSent() = client.getApi(Api.AccountAction.VerifyExistingEmail.CheckStatus)
    override suspend fun removeEmail(password: PasswordVerification) = client.postApi(Api.AccountAction.RemoveEmail, password)
    override suspend fun resetPassword(email: EmailAddress) = client.postApi(Api.AccountAction.ResetPassword, email)
    override suspend fun addEmail(value: EmailChange) = client.postApi(Api.AccountAction.ChangeEmail, value)
    override suspend fun changePassword(value: PasswordChange) = client.postApi(Api.AccountAction.ChangePassword, value)
}
