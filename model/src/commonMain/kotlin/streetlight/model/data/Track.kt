package streetlight.model.data

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
sealed interface Track {
    val patterns: List<Pattern>
    val placements: List<PatternId>
}

@Serializable
sealed interface Pattern {
    val patternId: PatternId
    val length: Beats
}

@JvmInline @Serializable
value class PatternId(val name: String)

// Chords

@Serializable
data class ChordTrack(
    override val patterns: List<Progression>,
    override val placements: List<PatternId>,
) : Track

/** The [notes] of a chord pattern, led by a [pickup] that sounds at the end of the measure before it. */
@Serializable
data class Progression(
    override val patternId: PatternId,
    val notes: List<ChordNote>,
    val pickup: List<ChordNote> = emptyList(),
    val measuresPerLine: Int = 4,
) : Pattern {
    override val length get() = Beats(notes.sumOf { it.beats.ticks })
}

/** A [chord] held for [beats], or a rest when [chord] is `null`. */
@Serializable
data class ChordNote(
    val chord: Chord?,
    val beats: Beats,
)

// Melody

@Serializable
data class MelodyTrack(
    override val patterns: List<Phrase>,
    override val placements: List<PatternId>,
) : Track

/** The [notes] of a melody pattern, led by a [pickup] that sounds at the end of the measure before it. */
@Serializable
data class Phrase(
    override val patternId: PatternId,
    val notes: List<MelodyNote>,
    val pickup: List<MelodyNote> = emptyList(),
    val measuresPerLine: Int = 4,
) : Pattern {
    override val length get() = Beats(notes.sumOf { it.beats.ticks })
}

/** A [pitch] held for [beats], or a rest when [pitch] is `null`. */
@Serializable
data class MelodyNote(
    val pitch: Pitch?,
    val beats: Beats,
)

/** A pitch counted in semitones as MIDI counts them, so middle C is 60. */
@JvmInline @Serializable
value class Pitch(val midi: Int)

// Lyrics

@Serializable
data class LyricTrack(
    override val patterns: List<Stanza>,
    override val placements: List<PatternId>,
) : Track

/** The [notes] of a lyric pattern, led by a [pickup] that sounds at the end of the measure before it. */
@Serializable
data class Stanza(
    override val patternId: PatternId,
    val notes: List<LyricNote>,
    val pickup: List<LyricNote> = emptyList(),
    val measuresPerLine: Int = 2,
) : Pattern {
    override val length get() = Beats(notes.sumOf { it.beats.ticks })
}

/** A syllable or word of [text] sung for [beats], or a rest when [text] is `null`. */
@Serializable
data class LyricNote(
    val text: String?,
    val beats: Beats,
)

// Drums

@Serializable
data class DrumTrack(
    override val patterns: List<Groove>,
    override val placements: List<PatternId>,
) : Track

@Serializable
data class Groove(
    override val patternId: PatternId,
    override val length: Beats,
    val hits: List<DrumHit>,
) : Pattern

@Serializable
data class DrumHit(
    val piece: DrumPiece,
    val onset: Beats,
)

enum class DrumPiece {
    Kick,
    Snare,
    HiHat,
    Crash,
    Ride,
    Tom,
}