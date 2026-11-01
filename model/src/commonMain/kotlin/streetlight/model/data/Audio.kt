package streetlight.model.data

import kampfire.model.Url
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.time.Duration
import kotlin.uuid.Uuid

/** A stored audio file, Opus in an mp4 container, with its [duration] and the details shown with it. */
@Serializable
@SerialName("audio")
data class Audio(
    val audioId: AudioId,
    val url: Url,
    val name: String?,
    val description: String?,
    val duration: Duration,
    val sizeBytes: Long,
): MediaMeta

@Serializable
@JvmInline
value class AudioId(override val value: Uuid): RecordId {
    override fun toString() = value.toString()

    companion object {
        fun random() = AudioId(Uuid.random())
    }
}
