package streetlight.web.io

import kampfire.model.Outcome
import streetlight.model.Api
import streetlight.model.data.EntityFeed
import streetlight.model.data.FeedRequest

/** The calls of `Api.Feeds`. */
interface FeedClient {
    suspend fun readFeed(request: FeedRequest): Outcome<EntityFeed>
}

class BrowserFeedClient(private val client: FetchClient): FeedClient {
    override suspend fun readFeed(request: FeedRequest) = client.postApi(Api.Feeds.Read, request)
}
