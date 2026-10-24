package streetlight.model.io

import kampfire.api.ApiTransport
import kampfire.api.getApi
import kampfire.api.postApi
import kampfire.model.Outcome
import kampfire.model.Url
import streetlight.model.Api
import streetlight.model.data.SellerOnboarding
import streetlight.model.data.SellerStatus

/** The calls of `Api.Sellers`. */
class SellerClient(private val transport: ApiTransport) {
    suspend fun onboard(onboarding: SellerOnboarding): Outcome<Url> = transport.postApi(Api.Sellers.Onboard, onboarding)
    suspend fun readStatus(): Outcome<SellerStatus?> = transport.getApi(Api.Sellers.ReadStatus)
}
