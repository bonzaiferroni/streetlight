package streetlight.model.io

import kampfire.api.ApiTransport
import kampfire.api.EmailAddress
import kampfire.api.Slug
import kampfire.api.UserApi
import kampfire.api.Username
import kampfire.api.getApi
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

/** The calls of `Api.Status`. */
class StatusClient(private val transport: ApiTransport) {
    suspend fun feedSiteStatusFeed(resolution: MetricResolution): Outcome<SiteStatusFeed> =
        transport.getApi(Api.Status.Feed) {
            writeParam(it.resolution, resolution)
        }
    suspend fun readLastSiteStatus(resolution: MetricResolution): Outcome<SiteStatus> =
        transport.getApi(Api.Status.ReadLast) {
            writeParam(it.resolution, resolution)
        }
}
