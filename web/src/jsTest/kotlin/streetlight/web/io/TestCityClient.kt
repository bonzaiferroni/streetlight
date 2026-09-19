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

class TestCityClient: CityClient {
    override suspend fun readCity(slug: Slug): Outcome<City> = TODO()
    override suspend fun readTopCities(): Outcome<List<City>> = TODO()
    override suspend fun readCityPosts(slug: Slug): Outcome<List<FeedEntity>> = TODO()
    override suspend fun searchCity(query: String, country: String): Outcome<List<City>> = TODO()
}
