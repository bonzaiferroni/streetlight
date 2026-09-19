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

interface GalaxyClient {
    suspend fun createGalaxy(galaxy: GalaxyEdit): Outcome<Slug>
    suspend fun updateGalaxy(galaxy: GalaxyEdit): Outcome<Slug>
    suspend fun readTopGalaxies(): Outcome<List<Galaxy>>
    suspend fun readGalaxies(galaxyIds: List<GalaxyId>): Outcome<List<Galaxy>>
    suspend fun readUserGalaxies(): Outcome<List<Galaxy>>
    suspend fun readGalaxy(slug: Slug): Outcome<Galaxy>
    suspend fun readGalaxyConfig(slug: Slug): Outcome<GalaxyConfig>
    suspend fun readGalaxyContent(slug: Slug): Outcome<GalaxyContent>
    suspend fun readGalaxy(galaxyId: GalaxyId): Outcome<Galaxy>
    suspend fun readGalaxyStars(): Outcome<List<GalaxyId>>
}

class BrowserGalaxyClient(private val client: FetchClient): GalaxyClient {
    override suspend fun createGalaxy(galaxy: GalaxyEdit) = client.postApi(Api.Galaxies.CreateGalaxy, galaxy)
    override suspend fun updateGalaxy(galaxy: GalaxyEdit) = client.postApi(Api.Galaxies.UpdateGalaxy, galaxy)
    override suspend fun readTopGalaxies() = client.getApi(Api.Galaxies.Top)
    override suspend fun readGalaxies(galaxyIds: List<GalaxyId>) = client.postApi(Api.Galaxies.ReadGalaxies, galaxyIds)
    override suspend fun readUserGalaxies() = client.getApi(Api.Galaxies.ReadUserGalaxies)
    override suspend fun readGalaxy(slug: Slug) = client.getApi(Api.Galaxies.ReadGalaxySlug, slug)
    override suspend fun readGalaxyConfig(slug: Slug) = client.getApi(Api.Galaxies.ReadConfig, slug)
    override suspend fun readGalaxyContent(slug: Slug) = client.getApi(Api.Galaxies.ReadContent, slug)
    override suspend fun readGalaxy(galaxyId: GalaxyId) = client.getApi(Api.Galaxies.ReadGalaxyId, galaxyId)
    override suspend fun readGalaxyStars() = client.getApi(Api.Galaxies.ReadLights)
}
