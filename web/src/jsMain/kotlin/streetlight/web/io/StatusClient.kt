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

/** The calls of `Api.Status`. */
interface StatusClient {
    suspend fun feedSiteStatusFeed(resolution: MetricResolution): Outcome<SiteStatusFeed>
    suspend fun readLastSiteStatus(resolution: MetricResolution): Outcome<SiteStatus>
}

class BrowserStatusClient(private val client: FetchClient): StatusClient {
    override suspend fun feedSiteStatusFeed(resolution: MetricResolution) =
        client.getApi(Api.Status.Feed) {
            writeParam(it.resolution, resolution)
        }
    override suspend fun readLastSiteStatus(resolution: MetricResolution) =
        client.getApi(Api.Status.ReadLast) {
            writeParam(it.resolution, resolution)
        }
}
