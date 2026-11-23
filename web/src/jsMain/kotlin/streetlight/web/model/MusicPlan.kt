package streetlight.web.model

import streetlight.model.music.MusicSheet
import streetlight.model.music.NotationId
import streetlight.model.music.TimeSignature
import streetlight.model.music.TrackClip
import streetlight.model.music.TrackId
import streetlight.model.music.notationsOf
import streetlight.model.music.trackOf
import kotlin.math.max

/**
 * The [trackTones], [waveStarts] and [clicks] a playback sounds, each wave by the tick it starts at, at
 * [secondsPerTick].
 */
class MusicPlan(
    val trackTones: List<TrackTones>,
    val waveStarts: List<Pair<Int, MusicWave>>,
    val secondsPerTick: Double,
    val clicks: List<ClickTone> = emptyList(),
) {
    /** The tick of the first sound: a pickup's, a lead-in's or a wave's lead may sound before the first downbeat. */
    val firstTick = (trackTones.mapNotNull { it.firstTick } + clicks.map { it.tick } +
        waveStarts.map { (tick, musicWave) -> tick - (musicWave.leadSeconds / secondsPerTick).toInt() }).min()

    /** Where the last sound ends on the clock, with tick 0 at [startClock]. */
    fun endClockOf(startClock: Double): Double {
        val toneEnd = (trackTones
            .flatMap { tones -> tones.tones.map { it.startTick + it.ticks } + tones.drums.map { it.startTick } } +
            clicks.map { it.tick })
            .maxOrNull()?.let { startClock + it * secondsPerTick } ?: startClock
        val waveEnd = waveStarts.maxOfOrNull { (tick, musicWave) ->
            startClock + tick * secondsPerTick - musicWave.leadSeconds + musicWave.wave.durationSeconds
        } ?: startClock
        return max(toneEnd, waveEnd)
    }
}

/** A click of the sticks at [tick], [accent]ed on a downbeat. */
data class ClickTone(
    val tick: Int,
    val accent: Boolean,
)

/**
 * This plan led in before its downbeat: [guideMidis] held a measure on the voice of its first track when given, then
 * two measures counted on sticks in [time], with a click on every pulse through its first [ticks] while [isClickOn].
 *
 * The count runs at half the pace of the pulse, then on it: the first measure on each half when its pulses split in
 * two, as "1… 2…" in 4/4, or on its downbeat alone, as in 3/4 or 6/8; the second on every pulse.
 */
fun MusicPlan.withLeadIn(time: TimeSignature, ticks: Int, isClickOn: Boolean, guideMidis: List<Int>?): MusicPlan {
    val measure = time.measure.ticks
    val pulse = time.pulse.ticks
    val pulses = measure / pulse
    val firstMeasure = if (pulses > 2 && pulses % 2 == 0) listOf(0, measure / 2) else listOf(0)
    val secondMeasure = List(pulses) { measure + it * pulse }
    val count = (firstMeasure + secondMeasure).map { offset -> ClickTone(offset - 2 * measure, offset % measure == 0) }
    val through = if (!isClickOn) emptyList() else (0 until ticks step pulse).map { ClickTone(it, it % measure == 0) }
    val guide = guideMidis?.let { NoteTone(-3 * measure, measure, it) }
    val guided = if (guide == null || trackTones.isEmpty()) trackTones
        else listOf(trackTones.first().let { it.copy(tones = listOf(guide) + it.tones) }) + trackTones.drop(1)
    return MusicPlan(guided, waveStarts, secondsPerTick, clicks + count + through)
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
