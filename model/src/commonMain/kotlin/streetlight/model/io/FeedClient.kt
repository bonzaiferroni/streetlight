package streetlight.model.io

import kampfire.api.ApiTransport
import kampfire.api.postApi
import kampfire.model.Outcome
import streetlight.model.Api
import streetlight.model.data.EntityFeed
import streetlight.model.data.FeedRequest

/** The calls of `Api.Feeds`. */
class FeedClient(private val transport: ApiTransport) {
    suspend fun readFeed(request: FeedRequest): Outcome<EntityFeed> = transport.postApi(Api.Feeds.Read, request)
}
