package streetlight.web.integration

import kampfire.api.toMarkdown
import kampfire.model.Problem
import koala.dom.View
import streetlight.model.data.Feedback
import streetlight.model.data.FeedbackId
import streetlight.model.data.FeedbackType
import streetlight.model.data.Platform
import streetlight.web.io.ApiClient
import streetlight.web.io.TestApiClient
import streetlight.web.io.TestFeedbackClient
import streetlight.web.ui.viewFeedbackHub
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Instant

class FeedbackHubTest: ViewTest() {

    private val feedback = TestFeedbackClient()

    override fun api(): ApiClient = TestApiClient(feedback = feedback)

    @Test
    fun `a sent note reaches the client and clears the editor`() = runViewTest {
        val view = mount { viewFeedbackHub() }

        view.writeIn("feedback", NOTE)
        view.clickButton("Send")

        awaitUntil("the note to reach the client") { feedback.sent.isNotEmpty() }
        assertEquals(NOTE, feedback.sent.single().text.value)
        view.awaitText("Feedback Sent.")
        view.awaitEditorCleared("feedback")
    }

    @Test
    fun `a blank note is not sent`() = runViewTest {
        val view = mount { viewFeedbackHub() }

        view.clickButton("Send")

        assertTrue(feedback.sent.isEmpty(), "a blank note should not reach the client")
        assertFalse(view.showsText("Feedback Sent."), "a blank note should not report a sent note")
    }

    @Test
    fun `a failed send keeps the note in the editor`() = runViewTest {
        val view = mountWith(TestFeedbackClient(onCreate = { Problem(SERVER_PROBLEM) }))

        view.writeIn("feedback", NOTE)
        view.clickButton("Send")

        view.awaitText(SERVER_PROBLEM)
        assertTrue(view.editor("feedback").textContent?.contains(NOTE) == true, "the note should stay in the editor")
        assertFalse(view.showsText("Feedback Sent."), "a failed send should not report a sent note")
    }

    @Test
    fun `the public feed lists existing feedback`() = runViewTest {
        val view = mountWith(TestFeedbackClient(feed = mutableListOf(publicFeedback(NOTE))))

        view.awaitText(NOTE)
    }

    @Test
    fun `a note shared publicly is sent as public`() = runViewTest {
        val view = mount { viewFeedbackHub() }

        view.writeIn("feedback", NOTE)
        view.chooseIn("sharing", "Share Publicly")
        view.clickButton("Send")

        awaitUntil("the note to reach the client") { feedback.sent.isNotEmpty() }
        assertFalse(feedback.sent.single().isPrivate, "a note shared publicly should not be sent as private")
    }

    private fun mountWith(client: TestFeedbackClient): View {
        app = buildTestApp(scope, TestApiClient(feedback = client))
        return mount { viewFeedbackHub() }
    }
}

private const val NOTE = "The map runs slow on my phone."
private const val SERVER_PROBLEM = "The server could not take your feedback."

private fun publicFeedback(text: String) = Feedback(
    feedbackId = FeedbackId.random(),
    feedbackType = FeedbackType.General,
    username = null,
    text = text.toMarkdown(),
    platform = Platform.Web,
    isPrivate = false,
    updatedAt = Instant.fromEpochSeconds(0),
    createdAt = Instant.fromEpochSeconds(0),
)
