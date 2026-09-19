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

interface LocationClient {
    suspend fun readLocation(locationId: LocationId): Outcome<Location>
    suspend fun readLocation(slug: Slug): Outcome<Location>
    suspend fun readLocationContent(slug: Slug): Outcome<LocationContent>
    suspend fun readLocationUpdaterContent(slug: Slug): Outcome<LocationUpdaterContent>
    suspend fun parseLocation(request: ParseRequest): Outcome<LocationEdit>
    suspend fun readLocationsInBounds(bounds: GeoRect): Outcome<List<LocationInfo>>
    suspend fun searchLocations(query: String, city: String? = null, state: String? = null, limit: Int = 10): Outcome<List<Location>>
    suspend fun readLocationConfigContent(locationId: LocationId): Outcome<LocationConfigContent>
    suspend fun parseEventSchema(url: Url): Outcome<List<SelectorSchema>>
    suspend fun updateLocationConfig(edit: LocationConfig): Outcome<Unit>
    suspend fun uploadSchemas(schemas: UrlSchemas): Outcome<Unit>
    suspend fun createLocation(location: LocationEdit): Outcome<Location>
    suspend fun updateLocation(location: LocationEdit): Outcome<Location>
    suspend fun configSubdomain(config: SubdomainConfig): Outcome<Unit>
    suspend fun queryLocation(point: GeoPoint): Outcome<List<Location>>
}

class BrowserLocationClient(private val client: FetchClient): LocationClient {
    override suspend fun readLocation(locationId: LocationId) = client.getApi(Api.Locations, locationId)
    override suspend fun readLocation(slug: Slug) = client.getApi(Api.Locations.ReadLocation, slug)
    override suspend fun readLocationContent(slug: Slug) = client.getApi(Api.Locations.ReadContent, slug)
    override suspend fun readLocationUpdaterContent(slug: Slug) = client.getApi(Api.Locations.ReadUpdaterContent, slug)
    override suspend fun parseLocation(request: ParseRequest) = client.postApi(Api.Locations.ParseLocation, request)
    override suspend fun readLocationsInBounds(bounds: GeoRect) = client.postApi(Api.Locations.QueryBounds, bounds)
    override suspend fun searchLocations(query: String, city: String?, state: String?, limit: Int) =
        client.getApi(Api.Locations.Search) {
            writeParam(it.query, query)
            writeParam(it.city, city)
            writeParam(it.state, state)
            writeParam(it.limit, limit)
        }
    override suspend fun readLocationConfigContent(locationId: LocationId) = client.getApi(Api.Locations.ReadConfigContent, locationId)
    override suspend fun parseEventSchema(url: Url) = client.postApi(Api.Locations.ParseEventSchema, url)
    override suspend fun updateLocationConfig(edit: LocationConfig) = client.postApi(Api.Locations.UpdateConfig, edit)
    override suspend fun uploadSchemas(schemas: UrlSchemas) = client.postApi(Api.Locations.UploadSchemas, schemas)
    override suspend fun createLocation(location: LocationEdit) = client.postApi(Api.Locations.CreateLocation, location)
    override suspend fun updateLocation(location: LocationEdit) = client.postApi(Api.Locations.UpdateLocation, location)
    override suspend fun configSubdomain(config: SubdomainConfig) = client.postApi(Api.Locations.UpdateSubdomain, config)
    override suspend fun queryLocation(point: GeoPoint) = client.getApi(Api.Locations.QueryPoint, point.toQuery())
}
