package streetlight.model.io

import kampfire.api.ApiTransport
import kampfire.api.EmailAddress
import kampfire.api.Slug
import kampfire.api.UserApi
import kampfire.api.Username
import kampfire.api.getApi
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

/** The calls of `Api.Content`, for the content of routes that belong to no other node. */
class ContentClient(private val transport: ApiTransport) {
    suspend fun readHomeContent(feed: FeedType?): Outcome<HomeContent> = transport.getApi(Api.Content.Home) { endpoint ->
        feed?.let { writeParam(endpoint.feed, it) }
    }
    suspend fun readCityFeedContent(): Outcome<CityFeedContent> = transport.getApi(Api.Content.CityFeed)
    suspend fun readStarFeedContent(): Outcome<StarFeedContent> = transport.getApi(Api.Content.StarFeed)
    suspend fun readGalaxyFeedContent(feed: FeedType?): Outcome<GalaxyFeedContent> = transport.getApi(Api.Content.GalaxyFeed) { endpoint ->
        feed?.let { writeParam(endpoint.feed, it) }
    }
}
