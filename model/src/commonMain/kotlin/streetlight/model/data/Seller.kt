package streetlight.model.data

import kotlinx.serialization.Serializable

/** The state of a star's seller account, as shown to them. */
@Serializable
data class SellerStatus(
    val chargeStatus: CapabilityStatus,
    val payoutStatus: CapabilityStatus,
    val isAwaitingSeller: Boolean,
    val statusReason: String?,
)

/** A service that processes payments on a seller's behalf. */
enum class PaymentProvider {
    Stripe,
}

/** The terms of a seller's provider account: who pays fees, carries losses and collects requirements. */
enum class SellerConfiguration {
    Standard,
}

/** The state of a seller's ability to take charges or receive payouts. */
enum class CapabilityStatus {
    Active,
    Pending,
    Restricted,
    Unsupported,
}

/** A request to begin or continue onboarding as a seller, with the [country] used when an account is created. */
@Serializable
data class SellerOnboarding(
    val country: String,
)
