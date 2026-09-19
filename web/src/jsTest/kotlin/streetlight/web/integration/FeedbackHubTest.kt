package streetlight.web.integration

import kampfire.api.toMarkdown
import kampfire.model.CoreProblem
import kampfire.model.UIMessageType
import koala.dom.View
import streetlight.model.data.Feedback
import streetlight.model.data.FeedbackId
import streetlight.model.data.FeedbackType
import streetlight.model.data.Platform
import streetlight.web.io.ApiClient
import streetlight.web.io.TestApiClient
import streetlight.web.io.TestFeedbackClient
import streetlight.web.model.Toaster
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
        val view = mountWith(TestFeedbackClient(onCreate = { CoreProblem.Something }))

        view.writeIn("feedback", NOTE)
        view.clickButton("Send")

        view.awaitText(CoreProblem.Something.message)
        assertTrue(view.editor("feedback").textContent?.contains(NOTE) == true, "the note should stay in the editor")
        assertFalse(view.showsText("Feedback Sent."), "a failed send should not report a sent note")
    }

    @Test
    fun `the public feed lists existing feedback`() = runViewTest {
        val view = mountWith(TestFeedbackClient(feed = mutableListOf(publicFeedback(NOTE))))

        view.awaitText(NOTE)
    }

    @Test
    fun `a failed feed load is reported to the user`() = runViewTest {
        mountWith(TestFeedbackClient(onFeed = { CoreProblem.Something }))

        awaitUntil("the problem to reach the toaster") {
            app.get<Toaster>().stateNow.messages.any {
                it.text == CoreProblem.Something.message && it.messageType == UIMessageType.Error
            }
        }
    }

    @Test
    fun `a note shared publicly is sent as public`() = runViewTest {
        val view = mount { viewFeedbackHub() }

        view.chooseIn("sharing", "Share Publicly")
        view.sendNote()

        assertFalse(feedback.sent.single().isPrivate, "a note shared publicly should not be sent as private")
    }

    @Test
    fun `a note is sent privately unless the sender shares it`() = runViewTest {
        val view = mount { viewFeedbackHub() }

        view.sendNote()

        assertTrue(feedback.sent.single().isPrivate, "a note should be private by default")
    }

    @Test
    fun `a chosen feedback type reaches the client`() = runViewTest {
        val view = mount { viewFeedbackHub() }

        view.chooseIn("type", "Issue or Bug")
        view.sendNote()

        assertEquals(FeedbackType.Issue, feedback.sent.single().feedbackType)
    }

    @Test
    fun `a second note can be sent after the first`() = runViewTest {
        val view = mount { viewFeedbackHub() }
        view.sendNote()
        view.awaitEditorCleared("feedback")

        view.writeIn("feedback", SECOND_NOTE)
        view.clickButton("Send")

        awaitUntil("the second note to reach the client") { feedback.sent.size == 2 }
        assertEquals(SECOND_NOTE, feedback.sent.last().text.value)
    }

    @Test
    fun `two quick presses of send deliver one note`() = runViewTest {
        val view = mount { viewFeedbackHub() }

        view.writeIn("feedback", NOTE)
        view.clickButton("Send")
        view.clickButton("Send")

        view.awaitText("Feedback Sent.")
        assertEquals(1, feedback.sent.size, "the second press should have been dropped")
    }

    private suspend fun View.sendNote(note: String = NOTE) {
        writeIn("feedback", note)
        clickButton("Send")
        awaitUntil("the note to reach the client") { feedback.sent.isNotEmpty() }
    }

    private fun mountWith(client: TestFeedbackClient): View {
        app = buildTestApp(scope, TestApiClient(feedback = client))
        return mount { viewFeedbackHub() }
    }
}

private const val NOTE = "The map runs slow on my phone."
private const val SECOND_NOTE = "The list is empty after I reload."

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
