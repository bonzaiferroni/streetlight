package streetlight.web.integration

import kampfire.model.CoreProblem
import kampfire.model.Url
import koala.dom.View
import streetlight.model.data.CapabilityStatus
import streetlight.model.data.SellerStatus
import streetlight.model.io.ApiClient
import kampfire.api.TestTransport
import streetlight.model.io.SellerClient
import streetlight.model.testing.TestApiClient
import streetlight.model.testing.answerSellers
import streetlight.web.model.SellerConnector
import streetlight.web.model.toaster
import streetlight.web.view.stripeConnectForm
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class StripeConnectFormTest: ViewTest() {

    private val opened = mutableListOf<Url>()

    override fun api(): ApiClient = TestApiClient()

    @Test
    fun `a ready seller sees their status without opening the dialog`() = runViewTest {
        val view = mountWith { answerSellers(statusOf(CapabilityStatus.Active, statusReason = null)) }

        view.awaitText("Ready")
        assertFalse(view.showsText("Stripe Dashboard"), "the dialog content should stay hidden until it is opened")
    }

    @Test
    fun `a star who has not started selling is sent to the onboarding link`() = runViewTest {
        val view = mountWith { answerSellers() }

        view.awaitText("Not connected")
        view.clickButton("Manage")
        view.awaitText("Start selling")
        view.clickButton("Start selling")

        awaitUntil("the onboarding link to open") { opened.isNotEmpty() }
        assertEquals("https://connect.stripe.test/setup", opened.single().value)
    }

    @Test
    fun `a restricted seller sees the reason`() = runViewTest {
        val status = statusOf(CapabilityStatus.Restricted, statusReason = "rejected_other")
        val view = mountWith { answerSellers(status) }

        view.awaitText("Restricted")
        view.clickButton("Manage")
        view.awaitText("Reason: rejected_other")
        assertTrue(view.showsText("Update details"), "a restricted seller should be offered to update their details")
    }

    @Test
    fun `a seller under review is offered nothing to do`() = runViewTest {
        val view = mountWith { answerSellers(statusOf(CapabilityStatus.Pending, statusReason = null)) }

        view.awaitText("Under review")
        view.clickButton("Manage")
        view.awaitText("Stripe is reviewing your details")
        assertFalse(view.showsText("Continue setup"), "a seller under review should not be sent back to setup")
        assertFalse(view.showsText("Update details"), "a seller under review should not be asked for details")
    }

    @Test
    fun `a failed onboarding request shows its problem and opens nothing`() = runViewTest {
        val view = mountWith { answerSellers(onOnboard = { CoreProblem.Something }) }

        view.awaitText("Not connected")
        view.clickButton("Manage")
        view.awaitText("Start selling")
        view.clickButton("Start selling")

        view.awaitText(CoreProblem.Something.message)
        assertTrue(opened.isEmpty(), "a failed request should not leave the app")
    }

    private fun mountWith(configure: TestTransport.() -> Unit): View = mount {
        context(SellerConnector(contentScope, SellerClient(TestTransport(configure)), toaster) { opened.add(it) }) {
            stripeConnectForm()
        }
    }
}

private fun statusOf(chargeStatus: CapabilityStatus, statusReason: String?) = SellerStatus(
    chargeStatus = chargeStatus,
    payoutStatus = CapabilityStatus.Restricted,
    isAwaitingSeller = false,
    statusReason = statusReason,
)
