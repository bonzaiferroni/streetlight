package streetlight.web.io

import kampfire.model.Outcome
import kampfire.model.Url
import streetlight.model.Api
import streetlight.model.data.SellerStatus
import streetlight.model.data.SellerOnboarding

/** The calls of `Api.Sellers`. */
interface SellerClient {
    suspend fun onboard(onboarding: SellerOnboarding): Outcome<Url>
    suspend fun readStatus(): Outcome<SellerStatus?>
}

class BrowserSellerClient(private val client: FetchClient): SellerClient {
    override suspend fun onboard(onboarding: SellerOnboarding) = client.postApi(Api.Sellers.Onboard, onboarding)
    override suspend fun readStatus() = client.getApi(Api.Sellers.ReadStatus)
}
