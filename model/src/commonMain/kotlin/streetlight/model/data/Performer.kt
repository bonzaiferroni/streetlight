package streetlight.model.data

import kampfire.model.UserId
import kampfire.utils.randomUuidString
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
data class Performer(
    val performerId: PerformerId,
    val userId: UserId?,
    val name: String?,
    val songs: List<String>?,
    val createdAt: Instant,
)

@JvmInline
@Serializable
value class PerformerId(override val value: String): ProjectId {
    companion object {
        fun random(): PerformerId = PerformerId(randomUuidString())
    }
}

