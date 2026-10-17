package streetlight.web.io

import kampfire.model.Outcome
import streetlight.model.Api
import streetlight.model.data.Customer

/** The calls of `Api.Customers`. */
interface CustomerClient {
    suspend fun readMine(): Outcome<List<Customer>>
}

class BrowserCustomerClient(private val client: FetchClient): CustomerClient {
    override suspend fun readMine() = client.getApi(Api.Customers.ReadMine)
}
