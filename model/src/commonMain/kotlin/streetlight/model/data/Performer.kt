package streetlight.model.data

import kampfire.model.UserId
import kampfire.utils.randomUuidString
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
data class Performer(
    val performerId: PerformerId,
    val userId: UserId,
    val venmo: String,
    val stageName: String,
)

@JvmInline @Serializable
value class PerformerId(override val value: String): ProjectId {
    companion object { fun random() = PerformerId(randomUuidString()) }
}
