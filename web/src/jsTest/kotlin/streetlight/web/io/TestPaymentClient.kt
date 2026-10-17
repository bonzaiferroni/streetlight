package streetlight.web.io

import kampfire.model.Ok
import kampfire.model.Outcome
import streetlight.model.data.Payment
import streetlight.model.data.PaymentEdit

/** Holds the star's [payments]. */
class TestPaymentClient(
    val payments: List<Payment> = emptyList(),
): PaymentClient {
    override suspend fun readMine(): Outcome<List<Payment>> = Ok(payments)
    override suspend fun create(edit: PaymentEdit): Outcome<Payment> = TODO()
}
