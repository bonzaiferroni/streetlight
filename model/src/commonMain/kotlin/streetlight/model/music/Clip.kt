package streetlight.model.music

import kampfire.model.Url
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.uuid.Uuid

/**
 * A recorded take [bars] measures long, played at [bpm] in [time] and sounding in [soundingKey], whose downbeat falls
 * [leadSeconds] into the recording. The [name] is for human eyes, and the [url] is its public home once shared.
 */
@Serializable
data class Clip(
    val clipId: ClipId,
    val name: String,
    val bpm: Int,
    val time: TimeSignature,
    val soundingKey: Key,
    val bars: Int,
    val leadSeconds: Double,
    val url: Url? = null,
)

@JvmInline @Serializable
value class ClipId(val value: Uuid) {
    companion object { fun random() = ClipId(Uuid.random()) }
}
