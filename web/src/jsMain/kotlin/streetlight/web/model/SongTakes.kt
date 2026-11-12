package streetlight.web.model

import streetlight.model.music.Chroma
import streetlight.model.music.Clip
import streetlight.model.music.ClipId
import streetlight.model.music.Key
import streetlight.model.music.Mix
import streetlight.model.music.Music
import streetlight.model.music.SectionId
import streetlight.model.music.TrackSectionId
import streetlight.model.music.Track
import streetlight.model.music.lengthOf
import kotlin.math.ceil

/** The takes that play in place of tones, by the section of the track each is bound to, at the mix's [bpm]. */
class SongTakes(
    val bpm: Int,
    val takes: Map<TrackSectionId, Take>,
)

/** A decoded clip, whose downbeat falls [leadSeconds] into its [pcm]. */
class Take(
    val pcm: PcmSample,
    val leadSeconds: Double,
)

/** Reads the takes of a mix from [clipLibrary], keeping each clip it decodes for later plays. */
class TakeShelf(
    private val clipLibrary: ClipLibrary,
) {
    private val decoded = mutableMapOf<ClipId, PcmSample>()

    /** The clips of [mix] that still fit [song], decoded. A clip that no longer fits leaves its section to the tones. */
    suspend fun takesOf(song: StoredSong, mix: Mix): SongTakes {
        val takes = mix.clips.mapNotNull { binding ->
            val trackSectionId = binding.trackSectionId
            val track = song.tracks.firstOrNull { it.trackId == trackSectionId.trackId } ?: return@mapNotNull null
            val clip = clipLibrary.readClip(binding.clipId)
                ?.takeIf { it.fits(song.music, mix, track, trackSectionId.sectionId) } ?: return@mapNotNull null
            val pcm = decoded[clip.clipId]
                ?: clipLibrary.readPcm(clip.clipId)?.also { decoded[clip.clipId] = it } ?: return@mapNotNull null
            trackSectionId to Take(pcm, clip.leadSeconds)
        }.toMap()
        return SongTakes(mix.bpm, takes)
    }
}

/** Whether this clip still fits the section [sectionId] of [track] in [mix]: its tempo, time, key and bars. */
fun Clip.fits(music: Music, mix: Mix, track: Track, sectionId: SectionId): Boolean {
    val bars = barsOf(music, track, sectionId) ?: return false
    return bpm == mix.bpm && time == music.time && soundingKey == mix.soundingKeyOf(track) && this.bars == bars
}

/** The key [track] sounds in within this mix, its own capo applied. */
fun Mix.soundingKeyOf(track: Track) = Key(Chroma.of(soundingKey.tonic.value + (track.capo ?: 0)), soundingKey.mode)

/** The measures [sectionId] of [track] spans, a part measure counted whole, or `null` when the track lacks it. */
fun barsOf(music: Music, track: Track, sectionId: SectionId): Int? {
    val length = track.lengthOf(sectionId)?.ticks?.takeIf { it > 0 } ?: return null
    return ceil(length.toDouble() / music.time.measure.ticks).toInt()
}
