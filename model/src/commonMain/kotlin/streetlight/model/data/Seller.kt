package streetlight.model.data

import kotlinx.serialization.Serializable
import kotlin.time.Instant

/** A user's registration with a [PaymentProvider], through which they accept payments for services. */
@Serializable
data class Seller(
    val starId: StarId,
    val provider: PaymentProvider,
    val providerAccountId: String,
    val configuration: SellerConfiguration,
    val country: String,
    val chargeStatus: CapabilityStatus,
    val payoutStatus: CapabilityStatus,
    val isAwaitingSeller: Boolean,
    val defaultCurrency: String?,
    val statusReason: String?,
    val createdAt: Instant,
    val updatedAt: Instant,
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
