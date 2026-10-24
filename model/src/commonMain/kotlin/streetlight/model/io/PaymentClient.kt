package streetlight.model.io

import kampfire.api.ApiTransport
import kampfire.api.getApi
import kampfire.api.postApi
import kampfire.model.Outcome
import streetlight.model.Api
import streetlight.model.data.Payment
import streetlight.model.data.PaymentEdit

/** The calls of `Api.Payments`. */
class PaymentClient(private val transport: ApiTransport) {
    suspend fun readMine(): Outcome<List<Payment>> = transport.getApi(Api.Payments.ReadMine)
    suspend fun create(edit: PaymentEdit): Outcome<Payment> = transport.postApi(Api.Payments.Create, edit)
}
