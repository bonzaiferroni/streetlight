package streetlight.model.io

import kampfire.api.ApiTransport
import kampfire.api.getApi
import kampfire.api.postApi
import kampfire.model.Outcome
import streetlight.model.Api
import streetlight.model.data.Feedback
import streetlight.model.data.FeedbackEdit

/** The calls of `Api.Feedback`. */
class FeedbackClient(private val transport: ApiTransport) {
    suspend fun feedFeedback(): Outcome<List<Feedback>> = transport.getApi(Api.Feedback.Feed)
    suspend fun createFeedback(edit: FeedbackEdit): Outcome<Unit> = transport.postApi(Api.Feedback.Create, edit)
}
