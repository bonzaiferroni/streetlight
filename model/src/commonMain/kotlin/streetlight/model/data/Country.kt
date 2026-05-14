package streetlight.model.data

import kampfire.utils.randomUuidString
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
data class Country(
    val countryId: CountryId,
    val name: String,
)

@Serializable
@JvmInline
value class CountryId(override val value: String): ProjectId {
    companion object {
        fun random() = CountryId(randomUuidString())
    }
    override fun toString() = value
}
