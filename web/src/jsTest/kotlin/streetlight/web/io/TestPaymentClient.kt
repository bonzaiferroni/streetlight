package streetlight.web.io

import kampfire.model.Outcome
import streetlight.model.data.Payment
import streetlight.model.data.PaymentEdit

class TestPaymentClient: PaymentClient {
    override suspend fun create(edit: PaymentEdit): Outcome<Payment> = TODO()
}
