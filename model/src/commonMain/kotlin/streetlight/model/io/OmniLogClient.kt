package streetlight.model.io

import kampfire.api.ApiTransport
import kampfire.api.openEvents
import kotlinx.coroutines.flow.Flow
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
import kotlin.uuid.Uuid
import streetlight.model.Api
import streetlight.model.data.*

/** The event stream of the omni log. */
class OmniLogClient(private val transport: ApiTransport) {
    fun connectOmniLog(): Flow<OmniMessage> = transport.openEvents(Api.Omni.Log)
}
