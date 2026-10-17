package streetlight.web.io

import kampfire.model.Outcome
import streetlight.model.Api
import streetlight.model.data.Payment
import streetlight.model.data.PaymentEdit

/** The calls of `Api.Payments`. */
interface PaymentClient {
    suspend fun readMine(): Outcome<List<Payment>>
    suspend fun create(edit: PaymentEdit): Outcome<Payment>
}

class BrowserPaymentClient(private val client: FetchClient): PaymentClient {
    override suspend fun readMine() = client.getApi(Api.Payments.ReadMine)
    override suspend fun create(edit: PaymentEdit) = client.postApi(Api.Payments.Create, edit)
}
