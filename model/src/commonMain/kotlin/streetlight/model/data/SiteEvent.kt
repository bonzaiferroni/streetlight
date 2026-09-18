package streetlight.model.data

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable
data class SiteEvent(
    val siteEventId: SiteEventId,
    val label: String,
    val note: String?,
    val occurredAt: Instant,
    val createdAt: Instant,
)

@Serializable
@JvmInline
value class SiteEventId(override val value: Uuid): RecordId {
    override fun toString() = value.toString()

    companion object {
        fun random() = SiteEventId(Uuid.random())
    }
}
