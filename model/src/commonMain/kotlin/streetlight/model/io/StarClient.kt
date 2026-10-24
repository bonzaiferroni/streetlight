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

/** The calls of `Api.Stars`, for the signed-in user and user pages. */
class StarClient(private val transport: ApiTransport) {
    suspend fun readPendingEdits(): Outcome<List<EditLog>> = transport.getApi(Api.Stars.PendingEdits)
    suspend fun readStarContent(username: Username): Outcome<StarContent> =
        transport.getApi(Api.Stars.ReadStarContent) {
            writeParam(it.username, username)
        }
    suspend fun readAccount(): Outcome<Account> = transport.getApi(Api.Stars.ReadAccount)
    suspend fun validateLogin(): Outcome<Star> = transport.getApi(Api.Stars.ValidateLogin)
    suspend fun updateProfile(edit: StarEdit): Outcome<Star> = transport.postApi(Api.Stars.UpdateProfile, edit)
    suspend fun editStarLink(edit: EditLightRequest): Outcome<Boolean> = transport.postApi(Api.Stars.EditLight, edit)
    suspend fun readProfileDesign(): Outcome<ProfileConfig> = transport.getApi(Api.Stars.ReadProfileConfig)
    suspend fun createLead(lead: StarLead): Outcome<LeadId> = transport.postApi(Api.Stars.CreateLead, lead)
}
