package streetlight.web.integration

import kampfire.model.CoreProblem
import koala.dom.View
import koala.model.Portal
import streetlight.model.ui.FrontDeskRoute
import streetlight.model.ui.Screen
import streetlight.model.io.ApiClient
import kampfire.api.TestTransport
import streetlight.model.data.BugEdit
import streetlight.model.testing.TestApiClient
import streetlight.model.testing.answerBugs
import streetlight.web.view.viewBugReporter
import web.window.window
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BugReporterTest: ViewTest() {

    private val sentBugs = mutableListOf<BugEdit>()

    override fun api(): ApiClient = TestApiClient { answerBugs(sentBugs) }

    @Test
    fun `a reported bug reaches the client and clears the editor`() = runViewTest {
        val view = mount { viewBugReporter() }

        view.writeIn("bug", DESCRIPTION)
        view.clickButton("Send")

        awaitUntil("the report to reach the client") { sentBugs.isNotEmpty() }
        assertEquals(DESCRIPTION, sentBugs.single().description.value)
        view.awaitText("Bug Reported.")
        view.awaitEditorCleared("bug")
    }

    @Test
    fun `a blank report is not sent`() = runViewTest {
        val view = mount { viewBugReporter() }

        view.clickButton("Send")

        assertTrue(sentBugs.isEmpty(), "a blank report should not reach the client")
        assertFalse(view.showsText("Bug Reported."), "a blank report should not report a sent bug")
    }

    @Test
    fun `a failed send keeps the description in the editor`() = runViewTest {
        val view = mountWith { answerBugs(onReport = { CoreProblem.Something }) }

        view.writeIn("bug", DESCRIPTION)
        view.clickButton("Send")

        view.awaitText(CoreProblem.Something.message)
        assertTrue(view.editor("bug").textContent?.contains(DESCRIPTION) == true, "the description should stay in the editor")
        assertFalse(view.showsText("Bug Reported."), "a failed send should not report a sent bug")
    }

    @Test
    fun `two quick presses of send deliver one report`() = runViewTest {
        val view = mount { viewBugReporter() }

        view.writeIn("bug", DESCRIPTION)
        view.clickButton("Send")
        view.clickButton("Send")

        view.awaitText("Bug Reported.")
        assertEquals(1, sentBugs.size, "the second press should have been dropped")
    }

    @Test
    fun `a report carries the device agent and the screen and path`() = runViewTest {
        app.get<Portal>().go(FrontDeskRoute)
        val view = mount { viewBugReporter() }

        view.writeIn("bug", DESCRIPTION)
        view.clickButton("Send")

        awaitUntil("the report to reach the client") { sentBugs.isNotEmpty() }
        val sent = sentBugs.single()
        assertEquals(Screen.Feedback, sent.screen)
        assertEquals(window.location.pathname, sent.path)
        assertTrue(sent.deviceAgent?.contains("viewport=") == true, "the device agent should carry the viewport, was: ${sent.deviceAgent}")
    }

    private fun mountWith(configure: TestTransport.() -> Unit): View {
        app = buildTestApp(scope, TestApiClient(configure))
        return mount { viewBugReporter() }
    }
}

private const val DESCRIPTION = "The filters reset when I go back to the map."
