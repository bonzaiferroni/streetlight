package streetlight.model.testing

import kampfire.api.TestTransport
import kampfire.model.HttpProblem
import kampfire.model.Ok
import kampfire.model.Outcome
import kampfire.model.Url
import streetlight.model.Api
import streetlight.model.data.BugEdit
import streetlight.model.data.Customer
import streetlight.model.data.Feedback
import streetlight.model.data.FeedbackEdit
import streetlight.model.data.Location
import streetlight.model.data.Offering
import streetlight.model.data.OfferingEdit
import streetlight.model.data.OfferingId
import streetlight.model.data.Payment
import streetlight.model.data.SellerOnboarding
import streetlight.model.data.SellerStatus
import kotlin.uuid.Uuid

/** Answers bug reports, adding each to [sent] and answering with [onReport]. */
fun TestTransport.answerBugs(
    sent: MutableList<BugEdit> = mutableListOf(),
    onReport: (BugEdit) -> Outcome<Unit> = { Ok(Unit) },
) {
    answer(Api.Bugs.Report) { call ->
        val edit = call.sent ?: error("no bug report sent")
        sent.add(edit)
        onReport(edit)
    }
}

/** Answers the signed-in user's customers with [customers]. */
fun TestTransport.answerCustomers(customers: List<Customer> = emptyList()) {
    answer(Api.Customers.ReadMine) { Ok(customers) }
}

/** Answers the feedback feed with [onFeed], and new feedback by adding it to [sent] and answering with [onCreate]. */
fun TestTransport.answerFeedback(
    sent: MutableList<FeedbackEdit> = mutableListOf(),
    feed: List<Feedback> = emptyList(),
    onCreate: (FeedbackEdit) -> Outcome<Unit> = { Ok(Unit) },
    onFeed: () -> Outcome<List<Feedback>> = { Ok(feed) },
) {
    answer(Api.Feedback.Feed) { onFeed() }
    answer(Api.Feedback.Create) { call ->
        val edit = call.sent ?: error("no feedback sent")
        sent.add(edit)
        onCreate(edit)
    }
}

/** Answers location searches and matches from [locations], by name, address and city. */
fun TestTransport.answerLocations(locations: List<Location> = emptyList()) {
    answer(Api.Locations.Search) { call ->
        val query = call.param(Api.Locations.Search.query) ?: ""
        val limit = call.param(Api.Locations.Search.limit) ?: locations.size
        Ok(locations.filter { it.name?.contains(query, ignoreCase = true) == true }.take(limit))
    }
    answer(Api.Locations.Match) { call ->
        val name = call.param(Api.Locations.Match.name) ?: ""
        val address = call.param(Api.Locations.Match.address)
        val city = call.param(Api.Locations.Match.city)
        Ok(locations.filter {
            (name.isNotBlank() && it.name?.contains(name, ignoreCase = true) == true ||
                !address.isNullOrBlank() && it.address?.contains(address, ignoreCase = true) == true) &&
                (city.isNullOrBlank() || it.city?.contains(city, ignoreCase = true) == true)
        })
    }
}

/** Answers the offering calls over [offerings], creating and updating within it. */
fun TestTransport.answerOfferings(offerings: MutableList<Offering> = mutableListOf()) {
    answer(Api.Offerings.ReadMine) { Ok(offerings.toList()) }
    answer(Api.Offerings.Read) { call ->
        offerings.firstOrNull { it.offeringId == call.sent }?.let { Ok(it) } ?: HttpProblem.NotFound
    }
    answer(Api.Offerings.Create) { call ->
        val offering = (call.sent ?: error("no offering sent")).toOffering(OfferingId(Uuid.random()))
        offerings.add(offering)
        Ok(offering)
    }
    answer(Api.Offerings.Update) { call ->
        val edit = call.sent ?: error("no offering sent")
        val offeringId = edit.offeringId ?: error("offering id not found")
        val index = offerings.indexOfFirst { it.offeringId == offeringId }.takeIf { it >= 0 }
            ?: error("offering not found: $offeringId")
        val offering = edit.toOffering(offeringId)
        offerings[index] = offering
        Ok(offering)
    }
    answer(Api.Offerings.ReadSkillIds) { Ok(emptyList()) }
}

/** Answers the signed-in user's payments with [payments]. */
fun TestTransport.answerPayments(payments: List<Payment> = emptyList()) {
    answer(Api.Payments.ReadMine) { Ok(payments) }
}

/** Answers the seller's [status], and onboarding with [onOnboard]. */
fun TestTransport.answerSellers(
    status: SellerStatus? = null,
    onOnboard: (SellerOnboarding) -> Outcome<Url> = { Ok(Url("https://connect.stripe.test/setup")) },
) {
    answer(Api.Sellers.ReadStatus) { Ok(status) }
    answer(Api.Sellers.Onboard) { call -> onOnboard(call.sent ?: error("no onboarding sent")) }
}

private fun OfferingEdit.toOffering(offeringId: OfferingId) = Offering(
    offeringId = offeringId,
    title = title ?: error("title not found"),
    priceCents = priceCents ?: error("price not found"),
    currency = currency ?: "usd",
    platformCut = platformCut,
    isPublic = false,
    description = description,
    tags = tags.takeIf { it.isNotEmpty() },
    image = image,
)
