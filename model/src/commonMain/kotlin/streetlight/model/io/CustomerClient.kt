package streetlight.model.io

import kampfire.api.ApiTransport
import kampfire.api.getApi
import kampfire.model.Outcome
import streetlight.model.Api
import streetlight.model.data.Customer

/** The calls of `Api.Customers`. */
class CustomerClient(private val transport: ApiTransport) {
    suspend fun readMine(): Outcome<List<Customer>> = transport.getApi(Api.Customers.ReadMine)
}
