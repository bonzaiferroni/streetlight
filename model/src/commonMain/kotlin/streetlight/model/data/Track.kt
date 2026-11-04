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

@Serializable
data class Progression(
    override val patternId: PatternId,
    val notes: List<ChordNote>,
) : Pattern {
    override val length get() = Beats(notes.sumOf { it.beats.ticks })
}

/** A [chord] held for [beats], or a rest when [chord] is `null`. */
@Serializable
data class ChordNote(
    val chord: Chord?,
    val beats: Beats,
)

// Lyrics

@Serializable
data class LyricTrack(
    override val patterns: List<Stanza>,
    override val placements: List<PatternId>,
) : Track

@Serializable
data class Stanza(
    override val patternId: PatternId,
    override val length: Beats,
    val phrases: List<LyricPhrase>,
) : Pattern

@Serializable
data class LyricPhrase(
    val text: String,
    val onset: Beats,
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