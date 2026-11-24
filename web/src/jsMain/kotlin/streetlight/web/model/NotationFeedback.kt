package streetlight.web.model

import streetlight.model.music.ui.NotationLayout
import streetlight.model.music.ui.NoteSource
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

/** The [notes] of a measure as sung, one for each note of its layout with a source, `null` where it was missed. */
data class MeasureFeedback(
    val notes: List<NoteFeedback?>,
)

/** The [measures] of a [NotationLayout] as sung. */
data class NotationFeedback(
    val measures: List<MeasureFeedback>,
)

/**
 * The feedback on each of [layouts], the passes of a study starting [startTicks] into a take, from what [analyzer] read
 * of the take. A frame of the take falls [offsetSeconds] after the music's time, its notes raised [capo] semitones to
 * sound, at [secondsPerTick].
 *
 * The sung pitch is aligned to the notes by dynamic time warping, so a phrase sung early or late still meets its
 * notes. A note's onset is moved to the nearest attack when one is close.
 */
fun notationFeedbackOf(
    layouts: List<NotationLayout>,
    startTicks: List<Int>,
    capo: Int,
    analyzer: MusicAnalyzer,
    offsetSeconds: Double,
    secondsPerTick: Double,
): List<NotationFeedback> {
    val expected = layouts.flatMapIndexed { passIndex, layout ->
        layout.measures.flatMap { it.notes }.mapNotNull { it.source }.filter { it.chord == null }.distinct().map {
            ExpectedNote(passIndex, it, startTicks[passIndex] + it.tick, it.ticks, it.midi + capo)
        }
    }.sortedBy { it.startTick }
    val sung = noteFeedbackOf(expected, analyzer, offsetSeconds, secondsPerTick)
    return layouts.mapIndexed { passIndex, layout ->
        NotationFeedback(layout.measures.map { measure ->
            MeasureFeedback(measure.notes.mapNotNull { it.source }.map { sung[passIndex to it] })
        })
    }
}

/** A note a take is scored against: [source] in the pass at [passIndex], sounding [midi] from [startTick] for [ticks]. */
private data class ExpectedNote(
    val passIndex: Int,
    val source: NoteSource,
    val startTick: Int,
    val ticks: Int,
    val midi: Int,
)

/** The feedback on each of [expected] that was sung, by its pass and source. */
private fun noteFeedbackOf(
    expected: List<ExpectedNote>,
    analyzer: MusicAnalyzer,
    offsetSeconds: Double,
    secondsPerTick: Double,
): Map<Pair<Int, NoteSource>, NoteFeedback> {
    if (expected.isEmpty()) return emptyMap()
    val hop = analyzer.hopSeconds
    val firstSeconds = expected.first().startTick * secondsPerTick - BAND_SECONDS
    val lastSeconds = expected.maxOf { it.startTick + it.ticks } * secondsPerTick + BAND_SECONDS
    val frames = analyzer.frames.filter { it.seconds + offsetSeconds in firstSeconds..lastSeconds }
    if (frames.isEmpty()) return emptyMap()
    val times = frames.map { it.seconds + offsetSeconds }

    // the semitone sung in each frame, and the note expected at its time, NaN and -1 for none
    val sungSemitones = frames.map { frame ->
        frame.hertz?.takeIf { frame.confidence >= MIN_CONFIDENCE && frame.rms >= MIN_RMS }?.let(::semitoneOf) ?: Double.NaN
    }
    var cursor = 0
    val expectedIndices = times.map { seconds ->
        val tick = seconds / secondsPerTick
        while (cursor < expected.size && expected[cursor].startTick + expected[cursor].ticks <= tick) cursor++
        if (cursor < expected.size && expected[cursor].startTick <= tick) cursor else -1
    }

    val path = warpPathOf(frames.size, (BAND_SECONDS / hop).toInt()) { i, j ->
        costOf(expectedIndices[i].takeIf { it >= 0 }?.let { expected[it].midi }, sungSemitones[j])
    }
    val sungFrames = expected.indices.associateWith { mutableListOf<Int>() }
    path.forEach { (i, j) -> expectedIndices[i].takeIf { it >= 0 }?.let { sungFrames.getValue(it).add(j) } }
    val noteFrameCounts = expectedIndices.filter { it >= 0 }.groupingBy { it }.eachCount()
    val onsets = analyzer.onsets.map { it + offsetSeconds }

    return expected.indices.mapNotNull { index ->
        val note = expected[index]
        val voiced = sungFrames.getValue(index).distinct().filter { !sungSemitones[it].isNaN() }.sorted()
        val frameCount = noteFrameCounts[index] ?: 0
        if (voiced.isEmpty() || voiced.size < frameCount * MIN_VOICED_SHARE) return@mapNotNull null

        val voicedStart = times[voiced.first()]
        val start = onsets.minByOrNull { abs(it - voicedStart) }?.takeIf { abs(it - voicedStart) <= SNAP_SECONDS }
            ?: voicedStart
        val end = times[voiced.last()] + hop
        val trim = (voiced.size * TRIM_SHARE).toInt()
        val pitches = voiced.map { sungSemitones[it] }.sorted().let { it.subList(trim, it.size - trim) }
        val cents = foldCents((pitches[pitches.size / 2] - note.midi) * 100)
        (note.passIndex to note.source) to NoteFeedback(
            time = ((start - note.startTick * secondsPerTick) / secondsPerTick).roundToInt(),
            pitch = cents,
            ticks = ((end - start) / secondsPerTick).roundToInt(),
        )
    }.toMap()
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
