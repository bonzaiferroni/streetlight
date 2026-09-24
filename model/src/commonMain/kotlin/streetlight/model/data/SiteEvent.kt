package streetlight.model.data

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.time.Instant
import kotlin.uuid.Uuid

/** A moment worth marking on the site's status charts, such as a deploy. */
@Serializable
data class SiteEvent(
    val siteEventId: SiteEventId,
    val label: String,
    val time: Instant,
)

@Serializable
@JvmInline
value class SiteEventId(override val value: Uuid): RecordId {
    override fun toString() = value.toString()

    companion object {
        fun random() = SiteEventId(Uuid.random())
    }
}
