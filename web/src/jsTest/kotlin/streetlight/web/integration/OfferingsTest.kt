package streetlight.web.integration

import koala.dom.View
import streetlight.model.data.Offering
import streetlight.model.data.OfferingId
import streetlight.model.io.ApiClient
import streetlight.model.testing.TestApiClient
import streetlight.model.testing.answerOfferings
import streetlight.web.view.sellerDash
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.uuid.Uuid

class OfferingsTest: ViewTest() {

    private val offerings = mutableListOf<Offering>()

    override fun api(): ApiClient = TestApiClient { answerOfferings(offerings) }

    @Test
    fun `a star creates an offering and sees it in their list`() = runViewTest {
        val view = mountPanel()

        view.awaitText("Nothing offered yet.")
        view.clickButton("New")
        view.awaitText("New Offering")
        view.typeIn("title", "Website Build")
        view.typeIn("price", "500")
        view.clickButton("Save")

        view.awaitText("Website Build")
        assertEquals("Website Build", offerings.single().title, "the offering should be created")
    }

    @Test
    fun `a star edits an offering and the list shows the change`() = runViewTest {
        offerings.add(offeringOf("Website Build"))
        val view = mountPanel()

        view.awaitText("Website Build")
        view.clickButton("Edit")
        view.awaitText("Edit Offering")
        view.typeIn("title", "Site Rescue")
        view.clickButton("Save")

        view.awaitText("Site Rescue")
        assertFalse(view.showsText("Website Build"), "the list should show the new title in place of the old")
        assertEquals(1, offerings.size, "the edit should update the offering rather than add one")
    }

    @Test
    fun `an offering without a price is not sent`() = runViewTest {
        val view = mountPanel()

        view.awaitText("Nothing offered yet.")
        view.clickButton("New")
        view.awaitText("New Offering")
        view.typeIn("title", "Website Build")
        view.clickButton("Save")

        view.awaitText("missing: price")
        assertTrue(offerings.isEmpty(), "an offering without a price should not be created")
    }

    private fun mountPanel(): View = mount {
        sellerDash()
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
