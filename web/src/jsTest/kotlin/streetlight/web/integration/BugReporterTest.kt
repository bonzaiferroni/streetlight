package streetlight.web.integration

import kampfire.model.CoreProblem
import koala.dom.View
import koala.model.Portal
import streetlight.model.ui.FrontDeskRoute
import streetlight.model.ui.Screen
import streetlight.web.io.ApiClient
import streetlight.web.io.TestApiClient
import streetlight.web.io.TestBugClient
import streetlight.web.ui.viewBugReporter
import web.window.window
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BugReporterTest: ViewTest() {

    private val bug = TestBugClient()

    override fun api(): ApiClient = TestApiClient(bug = bug)

    @Test
    fun `a reported bug reaches the client and clears the editor`() = runViewTest {
        val view = mount { viewBugReporter() }

        view.writeIn("bug", DESCRIPTION)
        view.clickButton("Send")

        awaitUntil("the report to reach the client") { bug.sent.isNotEmpty() }
        assertEquals(DESCRIPTION, bug.sent.single().description.value)
        view.awaitText("Bug Reported.")
        view.awaitEditorCleared("bug")
    }

    @Test
    fun `a blank report is not sent`() = runViewTest {
        val view = mount { viewBugReporter() }

        view.clickButton("Send")

        assertTrue(bug.sent.isEmpty(), "a blank report should not reach the client")
        assertFalse(view.showsText("Bug Reported."), "a blank report should not report a sent bug")
    }

    @Test
    fun `a failed send keeps the description in the editor`() = runViewTest {
        val view = mountWith(TestBugClient(onReport = { CoreProblem.Something }))

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
        assertEquals(1, bug.sent.size, "the second press should have been dropped")
    }

    @Test
    fun `a report carries the device agent and the screen and path`() = runViewTest {
        app.get<Portal>().go(FrontDeskRoute)
        val view = mount { viewBugReporter() }

        view.writeIn("bug", DESCRIPTION)
        view.clickButton("Send")

        awaitUntil("the report to reach the client") { bug.sent.isNotEmpty() }
        val sent = bug.sent.single()
        assertEquals(Screen.Feedback, sent.screen)
        assertEquals(window.location.pathname, sent.path)
        assertTrue(sent.deviceAgent?.contains("viewport=") == true, "the device agent should carry the viewport, was: ${sent.deviceAgent}")
    }

    private fun mountWith(client: TestBugClient): View {
        app = buildTestApp(scope, TestApiClient(bug = client))
        return mount { viewBugReporter() }
    }
}

private const val DESCRIPTION = "The filters reset when I go back to the map."
