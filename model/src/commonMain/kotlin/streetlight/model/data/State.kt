package streetlight.model.data

import kampfire.utils.randomUuidString
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
data class State(
    val stateId: StateId,
    val countryId: CountryId,
    val name: String,
)

@Serializable
@JvmInline
value class StateId(val value: Int)
