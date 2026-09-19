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

class TestLocationClient: LocationClient {
    override suspend fun readLocation(locationId: LocationId): Outcome<Location> = TODO()
    override suspend fun readLocation(slug: Slug): Outcome<Location> = TODO()
    override suspend fun readLocationContent(slug: Slug): Outcome<LocationContent> = TODO()
    override suspend fun readLocationUpdaterContent(slug: Slug): Outcome<LocationUpdaterContent> = TODO()
    override suspend fun parseLocation(request: ParseRequest): Outcome<LocationEdit> = TODO()
    override suspend fun readLocationsInBounds(bounds: GeoRect): Outcome<List<LocationInfo>> = TODO()
    override suspend fun searchLocations(query: String, city: String?, state: String?, limit: Int): Outcome<List<Location>> = TODO()
    override suspend fun readLocationConfigContent(locationId: LocationId): Outcome<LocationConfigContent> = TODO()
    override suspend fun parseEventSchema(url: Url): Outcome<List<SelectorSchema>> = TODO()
    override suspend fun updateLocationConfig(edit: LocationConfig): Outcome<Unit> = TODO()
    override suspend fun uploadSchemas(schemas: UrlSchemas): Outcome<Unit> = TODO()
    override suspend fun createLocation(location: LocationEdit): Outcome<Location> = TODO()
    override suspend fun updateLocation(location: LocationEdit): Outcome<Location> = TODO()
    override suspend fun configSubdomain(config: SubdomainConfig): Outcome<Unit> = TODO()
    override suspend fun queryLocation(point: GeoPoint): Outcome<List<Location>> = TODO()
}
