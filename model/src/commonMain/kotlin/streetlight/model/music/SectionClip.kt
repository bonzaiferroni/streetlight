package streetlight.model.music

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.uuid.Uuid

/**
 * A recorded take of the section [sectionId] of the track [trackId], kept as the clip [clipId], whose downbeat falls
 * [leadSeconds] into the recording.
 */
@Serializable
data class SectionClip(
    val trackId: TrackId,
    val sectionId: SectionId,
    val clipId: ClipId,
    val leadSeconds: Double,
)

@JvmInline @Serializable
value class ClipId(val value: Uuid) {
    companion object { fun random() = ClipId(Uuid.random()) }
}
