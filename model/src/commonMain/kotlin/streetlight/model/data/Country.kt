package streetlight.model.data

import kampfire.utils.randomUuidString
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

/** A country, as cities are grouped under it. */
@Serializable
data class Country(
    val countryId: CountryId,
    val name: String,
)

@Serializable
@JvmInline
value class CountryId(val value: Int)
