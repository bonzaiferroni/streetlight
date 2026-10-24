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

/** The calls of `Api.Locations`. */
class LocationClient(private val transport: ApiTransport) {
    suspend fun readLocation(locationId: LocationId): Outcome<Location> = transport.getApi(Api.Locations, locationId)
    suspend fun readLocation(slug: Slug): Outcome<Location> = transport.getApi(Api.Locations.ReadLocation, slug)
    suspend fun readLocationContent(slug: Slug): Outcome<LocationContent> = transport.getApi(Api.Locations.ReadContent, slug)
    suspend fun readLocationUpdaterContent(slug: Slug): Outcome<LocationUpdaterContent> = transport.getApi(Api.Locations.ReadUpdaterContent, slug)
    suspend fun readLocationsInBounds(bounds: GeoRect): Outcome<List<LocationInfo>> = transport.postApi(Api.Locations.QueryBounds, bounds)
    suspend fun searchLocations(query: String, city: String? = null, state: String? = null, limit: Int = 10): Outcome<List<Location>> =
        transport.getApi(Api.Locations.Search) {
            writeParam(it.query, query)
            writeParam(it.city, city)
            writeParam(it.state, state)
            writeParam(it.limit, limit)
        }
    suspend fun matchLocations(name: String, address: String?, city: String?): Outcome<List<Location>> =
        transport.getApi(Api.Locations.Match) {
            writeParam(it.name, name)
            writeParam(it.address, address)
            writeParam(it.city, city)
        }
    suspend fun readLocationConfigContent(locationId: LocationId): Outcome<LocationConfigContent> = transport.getApi(Api.Locations.ReadConfigContent, locationId)
    suspend fun parseEventSchema(url: Url): Outcome<List<SelectorSchema>> = transport.postApi(Api.Locations.ParseEventSchema, url)
    suspend fun updateLocationConfig(edit: LocationConfig): Outcome<Unit> = transport.postApi(Api.Locations.UpdateConfig, edit)
    suspend fun readGatherStatus(locationId: LocationId): Outcome<GatherEventStatus> = transport.getApi(Api.Locations.ReadGatherStatus, locationId)
    suspend fun updateParseConfig(config: ParseConfig): Outcome<Unit> = transport.postApi(Api.Locations.UpdateParseConfig, config)
    suspend fun uploadSchemas(schemas: UrlSchemas): Outcome<Unit> = transport.postApi(Api.Locations.UploadSchemas, schemas)
    suspend fun createLocation(location: LocationEdit): Outcome<Location> = transport.postApi(Api.Locations.CreateLocation, location)
    suspend fun updateLocation(location: LocationEdit): Outcome<Location> = transport.postApi(Api.Locations.UpdateLocation, location)
    suspend fun configSubdomain(config: SubdomainConfig): Outcome<Unit> = transport.postApi(Api.Locations.UpdateSubdomain, config)
    suspend fun checkSubdomain(config: SubdomainConfig): Outcome<Boolean> = transport.postApi(Api.Locations.CheckSubdomain, config)
    /** The content of the location at the subdomain [slug], as its LocationApp shows it. */
    suspend fun readSubdomainContent(slug: Slug): Outcome<LocationContent> = transport.getApi(Api.Locations.ReadSubdomainContent, slug)
    suspend fun queryLocation(point: GeoPoint): Outcome<List<Location>> = transport.getApi(Api.Locations.QueryPoint, point.toQuery())
    /** The locations the signed-in user hosts, by name. */
    suspend fun readHostedLocations(): Outcome<List<Location>> = transport.getApi(Api.Locations.ReadHosted)
    suspend fun readHostedConfigs(): Outcome<List<LocationConfigContent>> = transport.getApi(Api.Locations.ReadHostedConfigs)
}
