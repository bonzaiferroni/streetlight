package streetlight.model.music

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.uuid.Uuid
import streetlight.model.data.RecordId
import streetlight.model.data.StarId

/**
 * A way of playing the music [musicId] at [bpm] in [soundingKey], with clips bound to its sections by [SectionClip]. It
 * is seeded from the music and keeps its own tempo and key after, so its clips still fit when the music drifts. The
 * [hidden] sections are left out of its overview. Owned by [starId], or by the device when `null`.
 */
@Serializable
data class Mix(
    val mixId: MixId,
    val musicId: MusicId,
    val starId: StarId?,
    val name: String,
    val bpm: Int,
    val soundingKey: Key,
    val hidden: List<TrackSectionId> = emptyList(),
)

/** Binds the clip [clipId] to the track's section [trackSectionId] in the mix [mixId], one clip to each. */
@Serializable
data class SectionClip(
    val mixId: MixId,
    val trackSectionId: TrackSectionId,
    val clipId: ClipId,
)

@JvmInline @Serializable
value class MixId(override val value: Uuid): RecordId {
    companion object { fun random() = MixId(Uuid.random()) }
}

/** A new mix named [name] of [music], at its tempo and sounding key, owned by [starId]. */
fun mixOf(music: Music, name: String, starId: StarId? = null) =
    Mix(MixId.random(), music.musicId, starId, name, music.bpm, music.soundingKey)

/** The key the music sounds in, its [Music.capo] applied. */
val Music.soundingKey get() = Key(Chroma.of(key.tonic.value + (capo ?: 0)), key.mode)
