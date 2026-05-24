package streetlight.model.data

import kotlin.time.Instant
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.uuid.Uuid

@Serializable
data class Guest(
    val guestId: GuestId,
    val starId: StarId?,
    val name: String?,
    val songs: List<String>?,
    val createdAt: Instant,
)

@JvmInline
@Serializable
value class GuestId(override val value: Uuid): RecordId {
    companion object {
        fun random(): GuestId = GuestId(Uuid.random())
    }
}

