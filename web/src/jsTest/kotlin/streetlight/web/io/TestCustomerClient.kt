package streetlight.web.io

import kampfire.model.Ok
import kampfire.model.Outcome
import streetlight.model.data.Customer

/** Holds the star's [customers]. */
class TestCustomerClient(
    val customers: List<Customer> = emptyList(),
): CustomerClient {
    override suspend fun readMine(): Outcome<List<Customer>> = Ok(customers)
}
