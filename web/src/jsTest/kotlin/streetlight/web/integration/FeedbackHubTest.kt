package streetlight.web.integration

import streetlight.web.io.ApiClient
import streetlight.web.io.TestApiClient
import streetlight.web.io.TestFeedbackClient
import streetlight.web.ui.viewFeedbackHub
import kotlin.test.Test
import kotlin.test.assertEquals

class FeedbackHubTest: ViewTest() {

    private val feedback = TestFeedbackClient()

    override fun api(): ApiClient = TestApiClient(feedback = feedback)

    @Test
    fun `a sent note reaches the client and clears the editor`() = runViewTest {
        val view = mount { viewFeedbackHub() }
        val note = "The map runs slow on my phone."

        view.writeIn("feedback", note)
        view.clickButton("Send")

        awaitUntil("the note to reach the client") { feedback.sent.isNotEmpty() }
        assertEquals(note, feedback.sent.single().text.value)
        view.awaitText("Feedback Sent.")
        view.awaitEditorCleared("feedback")
    }
}
