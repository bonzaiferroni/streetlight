package streetlight.web.model

import streetlight.model.music.ui.NotationEvent
import streetlight.model.music.ui.NoteEvent
import streetlight.model.music.ui.TrackSource
import kotlin.math.abs
import kotlin.math.log2
import kotlin.math.roundToInt

/**
 * A note as it was sung: its onset [time] ticks from where the notation places it, held [ticks], at [pitch] cents from
 * its sounding pitch, folded within half an octave so a part sung an octave away scores on its notes.
 */
data class NoteFeedback(
    val time: Int,
    val pitch: Double,
    val ticks: Int,
)

/** Whether this event is a note a take is scored against: a pitch, not a rest, a chord or a syllable. */
val NotationEvent.isScored get() = this is NoteEvent && midi != null

/**
 * The feedback on each event of [track], the track of a study a take was sung to, from what [analyzer] read of the
 * take: one for each event, `null` where it was missed or is not [isScored]. A frame of the take falls
 * [offsetSeconds] after the music's time, the notes raised [capo] semitones to sound, at [secondsPerTick].
 *
 * The sung pitch is aligned to the notes by dynamic time warping, so a phrase sung early or late still meets its
 * notes. A note's onset is moved to the nearest attack when one is close.
 */
fun notationFeedbackOf(
    track: TrackSource,
    capo: Int,
    analyzer: MusicAnalyzer,
    offsetSeconds: Double,
    secondsPerTick: Double,
): List<NoteFeedback?> {
    val scored = track.events.filter { it.isScored }.filterIsInstance<NoteEvent>()
    val sung = noteFeedbackOf(scored, capo, analyzer, offsetSeconds, secondsPerTick)
    var next = 0
    return track.events.map { if (it.isScored) sung[next++] else null }
}

/** The feedback on each of [expected], raised [capo] semitones to sound, `null` where it was missed. */
private fun noteFeedbackOf(
    expected: List<NoteEvent>,
    capo: Int,
    analyzer: MusicAnalyzer,
    offsetSeconds: Double,
    secondsPerTick: Double,
): List<NoteFeedback?> {
    val missed = List(expected.size) { null }
    if (expected.isEmpty()) return missed
    val hop = analyzer.hopSeconds
    val firstSeconds = expected.first().tick * secondsPerTick - BAND_SECONDS
    val lastSeconds = expected.maxOf { it.tick + it.beats.ticks } * secondsPerTick + BAND_SECONDS
    val frames = analyzer.frames.filter { it.seconds + offsetSeconds in firstSeconds..lastSeconds }
    if (frames.isEmpty()) return missed
    val times = frames.map { it.seconds + offsetSeconds }

    // the semitone sung in each frame, and the note expected at its time, NaN and -1 for none
    val sungSemitones = frames.map { frame ->
        frame.hertz?.takeIf { frame.confidence >= MIN_CONFIDENCE && frame.rms >= MIN_RMS }?.let(::semitoneOf) ?: Double.NaN
    }
    var cursor = 0
    val expectedIndices = times.map { seconds ->
        val tick = seconds / secondsPerTick
        while (cursor < expected.size && expected[cursor].tick + expected[cursor].beats.ticks <= tick) cursor++
        if (cursor < expected.size && expected[cursor].tick <= tick) cursor else -1
    }

    val path = warpPathOf(frames.size, (BAND_SECONDS / hop).toInt()) { i, j ->
        costOf(expectedIndices[i].takeIf { it >= 0 }?.let { expected[it].midi?.plus(capo) }, sungSemitones[j])
    }
    val sungFrames = expected.indices.associateWith { mutableListOf<Int>() }
    path.forEach { (i, j) -> expectedIndices[i].takeIf { it >= 0 }?.let { sungFrames.getValue(it).add(j) } }
    val noteFrameCounts = expectedIndices.filter { it >= 0 }.groupingBy { it }.eachCount()
    val onsets = analyzer.onsets.map { it + offsetSeconds }

    return expected.indices.map { index ->
        val note = expected[index]
        val voiced = sungFrames.getValue(index).distinct().filter { !sungSemitones[it].isNaN() }.sorted()
        val frameCount = noteFrameCounts[index] ?: 0
        if (voiced.isEmpty() || voiced.size < frameCount * MIN_VOICED_SHARE) return@map null
        val midi = (note.midi ?: return@map null) + capo

        val voicedStart = times[voiced.first()]
        val start = onsets.minByOrNull { abs(it - voicedStart) }?.takeIf { abs(it - voicedStart) <= SNAP_SECONDS }
            ?: voicedStart
        val end = times[voiced.last()] + hop
        val trim = (voiced.size * TRIM_SHARE).toInt()
        val pitches = voiced.map { sungSemitones[it] }.sorted().let { it.subList(trim, it.size - trim) }
        val cents = foldCents((pitches[pitches.size / 2] - midi) * 100)
        NoteFeedback(
            time = ((start - note.tick * secondsPerTick) / secondsPerTick).roundToInt(),
            pitch = cents,
            ticks = ((end - start) / secondsPerTick).roundToInt(),
        )
    }
}

/**
 * The cheapest path through a [size] by [size] grid, kept within [band] of its diagonal, from its first cell to its
 * last, each step moving one along either side or both, by the cost [costOf] gives each cell.
 */
private fun warpPathOf(size: Int, band: Int, costOf: (Int, Int) -> Double): List<Pair<Int, Int>> {
    val width = 2 * band + 1
    val totals = DoubleArray(size * width) { Double.POSITIVE_INFINITY }
    fun totalOf(i: Int, j: Int) =
        if (i < 0 || j < 0 || j >= size || abs(j - i) > band) Double.POSITIVE_INFINITY else totals[i * width + j - i + band]

    for (i in 0 until size) {
        for (j in maxOf(0, i - band)..minOf(size - 1, i + band)) {
            val before = if (i == 0 && j == 0) 0.0 else minOf(totalOf(i - 1, j - 1), totalOf(i - 1, j), totalOf(i, j - 1))
            totals[i * width + j - i + band] = costOf(i, j) + before
        }
    }

    val path = mutableListOf(size - 1 to size - 1)
    var i = size - 1
    var j = size - 1
    while (i > 0 || j > 0) {
        val diagonal = totalOf(i - 1, j - 1)
        val up = totalOf(i - 1, j)
        val left = totalOf(i, j - 1)
        when {
            diagonal <= up && diagonal <= left -> { i--; j-- }
            up <= left -> i--
            else -> j--
        }
        path.add(i to j)
    }
    return path.asReversed()
}

/** The cost of singing [sung], a semitone or NaN for silence, where [expected] is the note's MIDI or `null` for a rest. */
private fun costOf(expected: Int?, sung: Double): Double = when {
    expected == null -> if (sung.isNaN()) 0.0 else REST_COST
    sung.isNaN() -> SILENCE_COST
    else -> minOf(abs(foldCents((sung - expected) * 100)) / 100, MAX_SEMITONES) / MAX_SEMITONES
}

/** The semitone of [hertz] on the MIDI scale. */
private fun semitoneOf(hertz: Double) = 69 + 12 * log2(hertz / 440)

/** [cents] folded within half an octave either way. */
private fun foldCents(cents: Double) = ((cents + 600).mod(1200.0)) - 600

// how far a phrase may stray from the beat and still meet its notes
private const val BAND_SECONDS = 1.0
private const val SNAP_SECONDS = 0.06
private const val MIN_CONFIDENCE = 0.8
private const val MIN_RMS = 0.01
private const val MIN_VOICED_SHARE = 0.3
private const val TRIM_SHARE = 0.15
private const val REST_COST = 0.5
private const val SILENCE_COST = 1.0
private const val MAX_SEMITONES = 3.0
