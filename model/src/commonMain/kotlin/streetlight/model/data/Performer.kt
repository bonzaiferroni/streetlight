package streetlight.model.data

import kampfire.utils.randomUuidString
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.uuid.Uuid

@Serializable
data class Performer(
    val performerId: PerformerId,
    val starId: StarId,
    val venmo: String,
    val stageName: String,
)

@JvmInline @Serializable
value class PerformerId(override val value: Uuid): ProjectId {
    companion object { fun random() = PerformerId(Uuid.random()) }
}
