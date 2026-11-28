package streetlight.web.model

import streetlight.model.music.TrackClip
import streetlight.model.music.TrackId
import streetlight.model.music.ui.MusicSource
import kotlin.math.max

/** The [layers] and [waveStarts] a playback sounds, each wave by the tick it starts at, at [secondsPerTick]. */
data class MusicPlan(
    val layers: List<ToneLayer>,
    val waveStarts: List<Pair<Int, MusicWave>>,
    val secondsPerTick: Double,
) {
    init {
        require(layers.any { it.isNotEmpty } || waveStarts.isNotEmpty()) { "a music plan needs something to sound" }
    }

    /** The tick of the first sound: a pickup's, a lead-in's or a wave's lead may sound before the first downbeat. */
    val firstTick = (layers.mapNotNull { it.firstTick } +
        waveStarts.map { (tick, musicWave) -> tick - (musicWave.leadSeconds / secondsPerTick).toInt() }).min()

    /** Where the last sound ends on the clock, with tick 0 at [startClock]. */
    fun endClockOf(startClock: Double): Double {
        val toneEnd = layers.flatMap { it.tones }.maxOfOrNull { it.endTick }?.let { startClock + it * secondsPerTick } ?: startClock
        val waveEnd = waveStarts.maxOfOrNull { (tick, musicWave) ->
            startClock + tick * secondsPerTick - musicWave.leadSeconds + musicWave.wave.durationSeconds
        } ?: startClock
        return max(toneEnd, waveEnd)
    }
}

/**
 * What the tracks [playing] of this source sound, with [musicWaves] in place of the tones they are bound to, or
 * `null` for nothing. A wave of a notation or arrangement starts each time it plays, and a whole track's at the start
 * of a source of the song.
 */
fun MusicSource.musicPlanOf(playing: Collection<TrackId>, musicWaves: Map<TrackClip, MusicWave>): MusicPlan? {
    val playingWaves = musicWaves.filterKeys { it.trackId in playing }
    val layers = musicTonesOf(playing, playingWaves.keys).filter { it.isNotEmpty }
    val waveStarts = playingWaves.flatMap { (trackClip, musicWave) ->
        val track = tracks.firstOrNull { it.trackId == trackClip.trackId } ?: return@flatMap emptyList()
        val starts = trackClip.notationId?.let { notationId -> track.spans.filter { it.notationId == notationId }.map { it.start } }
            ?: listOf(0).takeIf { targetId == null }.orEmpty()
        starts.map { it to musicWave }
    }
    return planOf(layers, waveStarts, secondsPerTick)
}

private fun planOf(layers: List<ToneLayer>, waveStarts: List<Pair<Int, MusicWave>>, secondsPerTick: Double) =
    if (layers.isEmpty() && waveStarts.isEmpty()) null else MusicPlan(layers, waveStarts, secondsPerTick)
