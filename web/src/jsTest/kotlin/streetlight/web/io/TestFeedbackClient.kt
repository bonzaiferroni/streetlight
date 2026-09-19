package streetlight.web.io

import kampfire.model.Ok
import kampfire.model.Outcome
import streetlight.model.data.Feedback
import streetlight.model.data.FeedbackEdit

class TestFeedbackClient(
    private val feed: MutableList<Feedback> = mutableListOf(),
    private val onCreate: (FeedbackEdit) -> Outcome<Unit> = { Ok(Unit) },
): FeedbackClient {

    val sent = mutableListOf<FeedbackEdit>()

    override suspend fun feedFeedback(): Outcome<List<Feedback>> = Ok(feed.toList())

    override suspend fun createFeedback(edit: FeedbackEdit): Outcome<Unit> {
        sent.add(edit)
        return onCreate(edit)
    }
}
