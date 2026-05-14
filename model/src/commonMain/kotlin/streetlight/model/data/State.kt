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
value class StateId(override val value: String): ProjectId {
    companion object {
        fun random() = StateId(randomUuidString())
    }
    override fun toString() = value
}
