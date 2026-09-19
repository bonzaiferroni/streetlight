package streetlight.web.io

import kampfire.model.Outcome
import streetlight.model.Api
import streetlight.model.data.Feedback
import streetlight.model.data.FeedbackEdit

interface FeedbackClient {
    suspend fun feedFeedback(): Outcome<List<Feedback>>
    suspend fun createFeedback(edit: FeedbackEdit): Outcome<Unit>
}

class BrowserFeedbackClient(private val client: FetchClient): FeedbackClient {
    override suspend fun feedFeedback() = client.getApi(Api.Feedback.Feed)
    override suspend fun createFeedback(edit: FeedbackEdit) = client.postApi(Api.Feedback.Create, edit)
}
