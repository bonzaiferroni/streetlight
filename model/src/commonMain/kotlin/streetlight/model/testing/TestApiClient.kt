package streetlight.model.testing

import kampfire.api.TestTransport
import streetlight.model.io.ApiClient

/**
 * An [ApiClient] over a [TestTransport] answering the ordinary case of each fixture, then the answers [configure]
 * gives, which replace them.
 */
fun TestApiClient(configure: TestTransport.() -> Unit = {}) = ApiClient(TestTransport {
    answerBugs()
    answerCustomers()
    answerFeedback()
    answerLocations()
    answerOfferings()
    answerPayments()
    answerSellers()
    configure()
})
