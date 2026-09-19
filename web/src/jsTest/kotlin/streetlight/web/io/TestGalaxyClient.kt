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

class TestGalaxyClient: GalaxyClient {
    override suspend fun createGalaxy(galaxy: GalaxyEdit): Outcome<Slug> = TODO()
    override suspend fun updateGalaxy(galaxy: GalaxyEdit): Outcome<Slug> = TODO()
    override suspend fun readTopGalaxies(): Outcome<List<Galaxy>> = TODO()
    override suspend fun readGalaxies(galaxyIds: List<GalaxyId>): Outcome<List<Galaxy>> = TODO()
    override suspend fun readUserGalaxies(): Outcome<List<Galaxy>> = TODO()
    override suspend fun readGalaxy(slug: Slug): Outcome<Galaxy> = TODO()
    override suspend fun readGalaxyConfig(slug: Slug): Outcome<GalaxyConfig> = TODO()
    override suspend fun readGalaxyContent(slug: Slug): Outcome<GalaxyContent> = TODO()
    override suspend fun readGalaxy(galaxyId: GalaxyId): Outcome<Galaxy> = TODO()
    override suspend fun readGalaxyStars(): Outcome<List<GalaxyId>> = TODO()
}
