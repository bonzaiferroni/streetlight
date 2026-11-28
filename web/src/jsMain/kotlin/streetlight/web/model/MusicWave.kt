package streetlight.web.model

import streetlight.model.music.Clip
import streetlight.model.music.ClipId
import streetlight.model.music.Music
import streetlight.model.music.MusicSheet
import streetlight.model.music.NotationId
import streetlight.model.music.Track
import streetlight.model.music.TrackClip
import streetlight.model.music.lengthOf
import streetlight.model.music.soundingKeyOf
import streetlight.model.studio.StudioDb
import kotlin.math.ceil

/** A decoded clip that plays along with its music, its downbeat falling [leadSeconds] into its [wave]. */
class MusicWave(
    val wave: Wave,
    val leadSeconds: Double,
)

/**
 * Reads the waves of the clips bound in [db] from [clipLibrary], keeping each clip it decodes for later plays.
 */
class WaveShelf(
    private val clipLibrary: ClipLibrary,
    private val db: StudioDb,
) {
    private val decoded = mutableMapOf<ClipId, Wave>()

    /**
     * The clips bound to the tracks and notations of [sheet] that still fit it, decoded, each by its binding. A clip
     * that no longer fits leaves its track or notation to the tones.
     */
    suspend fun musicWavesOf(sheet: MusicSheet): Map<TrackClip, MusicWave> =
        db.trackClip.readTrackClips(sheet.music.musicId).mapNotNull { trackClip ->
            val track = sheet.tracks.firstOrNull { it.trackId == trackClip.trackId } ?: return@mapNotNull null
            val clip = clipLibrary.readClip(trackClip.clipId)
                ?.takeIf { it.fits(sheet.music, track, trackClip.notationId) } ?: return@mapNotNull null
            val wave = decoded[clip.clipId]
                ?: clipLibrary.readWave(clip.clipId)?.also { decoded[clip.clipId] = it } ?: return@mapNotNull null
            trackClip to MusicWave(wave, clip.leadSeconds)
        }.toMap()
}

/**
 * Whether this clip still fits the notation [notationId] of [track] in [music], or the whole track when `null`: its
 * tempo, time, key and bars.
 */
fun Clip.fits(music: Music, track: Track, notationId: NotationId?): Boolean {
    val bars = barsOf(music, track, notationId) ?: return false
    return bpm == music.bpm && time == music.time && soundingKey == music.soundingKeyOf(track) && this.bars == bars
}

/**
 * The measures the notation [notationId] of [track] spans, a part measure counted whole, or the whole song's when
 * `null`. `null` when the track lacks the notation or the song is empty.
 */
fun barsOf(music: Music, track: Track, notationId: NotationId?): Int? {
    if (notationId == null) {
        return music.arrangement.sumOf { sectionId -> music.sections.firstOrNull { it.sectionId == sectionId }?.measures ?: 0 }
            .takeIf { it > 0 }
    }
    val length = track.lengthOf(notationId, music.time)?.ticks?.takeIf { it > 0 } ?: return null
    return ceil(length.toDouble() / music.time.measure.ticks).toInt()
}
