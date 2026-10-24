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

/** The calls that change a signed-in account's email and password, under `Api.AccountAction`. */
class AccountActionClient(private val transport: ApiTransport) {
    suspend fun verifyExistingEmail(): Outcome<Unit> = transport.postApi(Api.AccountAction.VerifyExistingEmail)
    suspend fun readEmailVerificationIsSent(): Outcome<Boolean> = transport.getApi(Api.AccountAction.VerifyExistingEmail.CheckStatus)
    suspend fun removeEmail(password: PasswordVerification): Outcome<Unit> = transport.postApi(Api.AccountAction.RemoveEmail, password)
    suspend fun resetPassword(email: EmailAddress): Outcome<Unit> = transport.postApi(Api.AccountAction.ResetPassword, email)
    suspend fun addEmail(value: EmailChange): Outcome<Unit> = transport.postApi(Api.AccountAction.ChangeEmail, value)
    suspend fun changePassword(value: PasswordChange): Outcome<Unit> = transport.postApi(Api.AccountAction.ChangePassword, value)
}
