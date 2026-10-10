package streetlight.web.io

import kampfire.model.Outcome
import streetlight.model.data.EntityFeed
import streetlight.model.data.FeedRequest

class TestFeedClient: FeedClient {
    override suspend fun readFeed(request: FeedRequest): Outcome<EntityFeed> = TODO()
}
