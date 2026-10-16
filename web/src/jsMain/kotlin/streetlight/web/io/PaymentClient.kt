package streetlight.web.io

import kampfire.model.Outcome
import streetlight.model.Api
import streetlight.model.data.Payment
import streetlight.model.data.PaymentEdit

/** The calls of `Api.Payments`. */
interface PaymentClient {
    suspend fun create(edit: PaymentEdit): Outcome<Payment>
}

class BrowserPaymentClient(private val client: FetchClient): PaymentClient {
    override suspend fun create(edit: PaymentEdit) = client.postApi(Api.Payments.Create, edit)
}
