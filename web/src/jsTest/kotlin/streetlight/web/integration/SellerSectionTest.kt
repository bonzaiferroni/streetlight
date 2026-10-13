package streetlight.web.integration

import kampfire.model.CoreProblem
import kampfire.model.Url
import koala.dom.View
import streetlight.model.data.CapabilityStatus
import streetlight.model.data.PaymentProvider
import streetlight.model.data.Seller
import streetlight.model.data.SellerConfiguration
import streetlight.model.data.StarId
import streetlight.web.io.ApiClient
import streetlight.web.io.TestApiClient
import streetlight.web.io.TestSellerClient
import streetlight.web.model.SellerEditor
import streetlight.web.model.toaster
import streetlight.web.view.sellerSection
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Instant
import kotlin.uuid.Uuid

class SellerSectionTest: ViewTest() {

    private val opened = mutableListOf<Url>()

    override fun api(): ApiClient = TestApiClient()

    @Test
    fun `a star who has not started selling is sent to the onboarding link`() = runViewTest {
        val view = mountWith(TestSellerClient())

        view.awaitText("Start selling")
        view.clickButton("Start selling")

        awaitUntil("the onboarding link to open") { opened.isNotEmpty() }
        assertEquals("https://connect.stripe.test/setup", opened.single().value)
    }

    @Test
    fun `a restricted seller sees the reason`() = runViewTest {
        val seller = sellerOf(CapabilityStatus.Restricted, statusReason = "rejected_other")
        val view = mountWith(TestSellerClient(seller))

        view.awaitText("Reason: rejected_other")
        assertTrue(view.showsText("Update details"), "a restricted seller should be offered to update their details")
    }

    @Test
    fun `a seller under review is offered nothing to do`() = runViewTest {
        val view = mountWith(TestSellerClient(sellerOf(CapabilityStatus.Pending, statusReason = null)))

        view.awaitText("Stripe is reviewing your details")
        assertFalse(view.showsText("Continue setup"), "a seller under review should not be sent back to setup")
        assertFalse(view.showsText("Update details"), "a seller under review should not be asked for details")
    }

    @Test
    fun `a failed onboarding request shows its problem and opens nothing`() = runViewTest {
        val view = mountWith(TestSellerClient(onOnboard = { CoreProblem.Something }))

        view.awaitText("Start selling")
        view.clickButton("Start selling")

        view.awaitText(CoreProblem.Something.message)
        assertTrue(opened.isEmpty(), "a failed request should not leave the app")
    }

    private fun mountWith(sellers: TestSellerClient): View = mount {
        sellerSection(SellerEditor(contentScope, sellers, toaster) { opened.add(it) })
    }
}

private fun sellerOf(chargeStatus: CapabilityStatus, statusReason: String?) = Seller(
    starId = StarId(Uuid.random()),
    provider = PaymentProvider.Stripe,
    providerAccountId = "acct_test",
    configuration = SellerConfiguration.Standard,
    country = "US",
    chargeStatus = chargeStatus,
    payoutStatus = CapabilityStatus.Restricted,
    isAwaitingSeller = false,
    defaultCurrency = "usd",
    statusReason = statusReason,
    createdAt = Instant.fromEpochSeconds(0),
    updatedAt = Instant.fromEpochSeconds(0),
)
