package streetlight.web.integration

import koala.dom.View
import streetlight.model.data.Offering
import streetlight.model.data.OfferingId
import streetlight.web.io.ApiClient
import streetlight.web.io.TestApiClient
import streetlight.web.io.TestOfferingClient
import streetlight.web.view.sellerConfigPanel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.uuid.Uuid

class OfferingsTest: ViewTest() {

    private val offerings = TestOfferingClient()

    override fun api(): ApiClient = TestApiClient(offering = offerings)

    @Test
    fun `a star creates an offering and sees it in their list`() = runViewTest {
        val view = mountPanel()

        view.awaitText("Nothing offered yet.")
        view.clickButton("New offering")
        view.awaitText("New Offering")
        view.typeIn("title", "Website Build")
        view.typeIn("price", "500")
        view.clickButton("Save")

        view.awaitText("500.00 USD")
        assertEquals("Website Build", offerings.offerings.single().title, "the offering should be created")
    }

    @Test
    fun `a star edits an offering and the list shows the change`() = runViewTest {
        offerings.offerings.add(offeringOf("Website Build"))
        val view = mountPanel()

        view.awaitText("Website Build")
        view.clickButton("Edit")
        view.awaitText("Edit Offering")
        view.typeIn("title", "Site Rescue")
        view.clickButton("Save")

        view.awaitText("Site Rescue")
        assertFalse(view.showsText("Website Build"), "the list should show the new title in place of the old")
        assertEquals(1, offerings.offerings.size, "the edit should update the offering rather than add one")
    }

    @Test
    fun `an offering without a price is not sent`() = runViewTest {
        val view = mountPanel()

        view.awaitText("Nothing offered yet.")
        view.clickButton("New offering")
        view.awaitText("New Offering")
        view.typeIn("title", "Website Build")
        view.clickButton("Save")

        view.awaitText("missing: price")
        assertTrue(offerings.offerings.isEmpty(), "an offering without a price should not be created")
    }

    private fun mountPanel(): View = mount {
        sellerConfigPanel()
    }
}

private fun offeringOf(title: String) = Offering(
    offeringId = OfferingId(Uuid.random()),
    title = title,
    priceCents = 50_000,
    currency = "usd",
    platformCut = 0,
    isPublic = false,
    description = null,
    tags = null,
    image = null,
)
