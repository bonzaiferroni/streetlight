package streetlight.model.data

import kotlinx.serialization.Serializable
import kotlin.time.Instant

/** A stored [audio] and the star who uploaded it. */
@Serializable
data class AudioRecord(
    val starId: StarId,
    val audio: Audio,
    val updatedAt: Instant,
    val createdAt: Instant,
)
