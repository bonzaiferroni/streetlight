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

interface CityClient {
    suspend fun readCity(slug: Slug): Outcome<City>
    suspend fun readTopCities(): Outcome<List<City>>
    suspend fun readCityPosts(slug: Slug): Outcome<List<FeedEntity>>
    suspend fun searchCity(query: String, country: String): Outcome<List<City>>
}

class BrowserCityClient(private val client: FetchClient): CityClient {
    override suspend fun readCity(slug: Slug) = client.getApi(Api.Cities.ReadCity, slug)
    override suspend fun readTopCities() = client.getApi(Api.Cities.ReadTopCities)
    override suspend fun readCityPosts(slug: Slug) = client.getApi(Api.Cities.ReadCityPosts, slug)
    override suspend fun searchCity(query: String, country: String) =
        client.getApi(Api.Cities.Search) {
            writeParam(it.query, query)
            writeParam(it.country, country)
        }
}
