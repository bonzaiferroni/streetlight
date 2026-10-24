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

/** The calls of `Api.Galaxies`. */
class GalaxyClient(private val transport: ApiTransport) {
    suspend fun createGalaxy(galaxy: GalaxyEdit): Outcome<Slug> = transport.postApi(Api.Galaxies.CreateGalaxy, galaxy)
    suspend fun updateGalaxy(galaxy: GalaxyEdit): Outcome<Slug> = transport.postApi(Api.Galaxies.UpdateGalaxy, galaxy)
    suspend fun readTopGalaxies(): Outcome<List<Galaxy>> = transport.getApi(Api.Galaxies.Top)
    suspend fun readGalaxies(galaxyIds: List<GalaxyId>): Outcome<List<Galaxy>> = transport.postApi(Api.Galaxies.ReadGalaxies, galaxyIds)
    suspend fun readUserGalaxies(): Outcome<List<Galaxy>> = transport.getApi(Api.Galaxies.ReadUserGalaxies)
    suspend fun readGalaxy(slug: Slug): Outcome<Galaxy> = transport.getApi(Api.Galaxies.ReadGalaxySlug, slug)
    suspend fun readGalaxyConfig(slug: Slug): Outcome<GalaxyConfig> = transport.getApi(Api.Galaxies.ReadConfig, slug)
    suspend fun readGalaxyContent(slug: Slug): Outcome<GalaxyContent> = transport.getApi(Api.Galaxies.ReadContent, slug)
    suspend fun readGalaxy(galaxyId: GalaxyId): Outcome<Galaxy> = transport.getApi(Api.Galaxies.ReadGalaxyId, galaxyId)
    suspend fun readGalaxyStars(): Outcome<List<GalaxyId>> = transport.getApi(Api.Galaxies.ReadLights)
}
