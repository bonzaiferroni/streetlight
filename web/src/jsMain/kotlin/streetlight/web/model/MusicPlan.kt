package streetlight.web.model

import streetlight.model.music.MusicSheet
import streetlight.model.music.NotationId
import streetlight.model.music.TrackClip
import streetlight.model.music.TrackId
import streetlight.model.music.notationsOf
import streetlight.model.music.trackOf
import kotlin.math.max

/** The [trackTones] and [waveStarts] a playback sounds, each wave by the tick it starts at, at [secondsPerTick]. */
class MusicPlan(
    val trackTones: List<TrackTones>,
    val waveStarts: List<Pair<Int, MusicWave>>,
    val secondsPerTick: Double,
) {
    /** The tick of the first sound: a pickup's or a wave's lead may sound before the first downbeat. */
    val firstTick = (trackTones.mapNotNull { it.firstTick } +
        waveStarts.map { (tick, musicWave) -> tick - (musicWave.leadSeconds / secondsPerTick).toInt() }).min()

    /** Where the last sound ends on the clock, with tick 0 at [startClock]. */
    fun endClockOf(startClock: Double): Double {
        val toneEnd = trackTones
            .flatMap { tones -> tones.tones.map { it.startTick + it.ticks } + tones.drums.map { it.startTick } }
            .maxOrNull()?.let { startClock + it * secondsPerTick } ?: startClock
        val waveEnd = waveStarts.maxOfOrNull { (tick, musicWave) ->
            startClock + tick * secondsPerTick - musicWave.leadSeconds + musicWave.wave.durationSeconds
        } ?: startClock
        return max(toneEnd, waveEnd)
    }
}

/**
 * What the tracks [playing] of this sheet sound through its arrangement, with [musicWaves] in place of the tones they
 * are bound to, or `null` for nothing.
 */
fun MusicSheet.musicPlanOf(playing: Collection<TrackId>, musicWaves: Map<TrackClip, MusicWave>): MusicPlan? {
    val playingTracks = tracks.filter { it.trackId in playing }
    val playingWaves = musicWaves.filterKeys { it.trackId in playing }
    val trackTones = musicTonesOf(playing, playingWaves.keys).filter { it.isSounding }
    // each time a wave's track plays its notation, placed by the notation's tick, and a whole track's at the start
    val waveStarts = playingWaves.flatMap { (trackClip, musicWave) ->
        val waveTrack = playingTracks.first { it.trackId == trackClip.trackId }
        val starts = trackClip.notationId?.let { notationStartsOf(music, waveTrack, it) } ?: listOf(0)
        starts.map { it to musicWave }
    }
    return planOf(trackTones, waveStarts, music.secondsPerTick)
}

/**
 * What the notation or arrangement [notationId] of this sheet sounds once from tick 0, with [musicWaves] in place of
 * the tones they are bound to, or `null` for nothing.
 */
fun MusicSheet.musicPlanOf(notationId: NotationId, musicWaves: Map<TrackClip, MusicWave>): MusicPlan? {
    val track = trackOf(notationId)
    var onset = 0
    val held = track.notationsOf(notationId).orEmpty().map { notation ->
        (notation.notationId to onset).also { onset += notation.length.ticks }
    }
    val starts = (listOf(notationId to 0) + held).distinct()
    val notationWaves = musicWaves.filterKeys { it.trackId == track.trackId && it.notationId != null }
    val waveStarts = notationWaves.flatMap { (trackClip, musicWave) ->
        starts.filter { it.first == trackClip.notationId }.map { it.second to musicWave }
    }
    val trackTones = listOf(notationTonesOf(notationId, notationWaves.keys)).filter { it.isSounding }
    return planOf(trackTones, waveStarts, music.secondsPerTick)
}

private fun planOf(trackTones: List<TrackTones>, waveStarts: List<Pair<Int, MusicWave>>, secondsPerTick: Double) =
    if (trackTones.isEmpty() && waveStarts.isEmpty()) null else MusicPlan(trackTones, waveStarts, secondsPerTick)
