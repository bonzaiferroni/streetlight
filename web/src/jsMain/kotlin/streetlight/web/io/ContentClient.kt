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

interface ContentClient {
    suspend fun readHomeContent(): Outcome<HomeContent>
    suspend fun readCityListContent(): Outcome<CityListContent>
}

class BrowserContentClient(private val client: FetchClient): ContentClient {
    override suspend fun readHomeContent() = client.getApi(Api.Content.Home)
    override suspend fun readCityListContent() = client.getApi(Api.Content.CityList)
}
