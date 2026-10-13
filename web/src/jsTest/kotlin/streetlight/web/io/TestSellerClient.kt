package streetlight.web.io

import kampfire.model.Ok
import kampfire.model.Outcome
import kampfire.model.Url
import streetlight.model.data.Seller
import streetlight.model.data.SellerOnboarding

class TestSellerClient(
    private val seller: Seller? = null,
    private val onOnboard: (SellerOnboarding) -> Outcome<Url> = { Ok(Url("https://connect.stripe.test/setup")) },
): SellerClient {
    override suspend fun onboard(onboarding: SellerOnboarding): Outcome<Url> = onOnboard(onboarding)
    override suspend fun readStatus(): Outcome<Seller?> = Ok(seller)
}
