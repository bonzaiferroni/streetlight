package streetlight.model.music

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

/**
 * A part played by a voice or instrument: its [sections], and its [arrangements] of sections into larger ones.
 *
 * The track sounds [capo] semitones higher than written, or lower for a negative one, on top of the [Music.capo] of
 * its song.
 */
@Serializable
sealed interface Track {
    val sections: List<Section>
    val arrangements: List<Arrangement>
    val capo: Int?
    val name: String?
}

@Serializable
sealed interface Section {
    val sectionId: SectionId
    val length: Beats
}

@JvmInline @Serializable
value class SectionId(val name: String)

/** A section made of other [sections] played in order, each a section or another arrangement. */
@Serializable
data class Arrangement(
    val sectionId: SectionId,
    val sections: List<SectionId>,
)

// Chords

@Serializable
data class ChordTrack(
    override val sections: List<Progression>,
    override val arrangements: List<Arrangement>,
    override val capo: Int? = null,
    override val name: String? = null,
) : Track

/** The [notes] of a chord section, led by a [pickup] that sounds at the end of the measure before it. */
@Serializable
data class Progression(
    override val sectionId: SectionId,
    val notes: List<ChordNote>,
    val pickup: List<ChordNote> = emptyList(),
    val measuresPerLine: Int = 4,
) : Section {
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
    override val sections: List<Phrase>,
    override val arrangements: List<Arrangement>,
    override val capo: Int? = null,
    override val name: String? = null,
) : Track

/** The [notes] of a melody section, led by a [pickup] that sounds at the end of the measure before it. */
@Serializable
data class Phrase(
    override val sectionId: SectionId,
    val notes: List<MelodyNote>,
    val pickup: List<MelodyNote> = emptyList(),
    val measuresPerLine: Int = 4,
) : Section {
    override val length get() = Beats(notes.sumOf { it.beats.ticks })
}

/** A note at [interval] held for [beats], or a rest when [interval] is `null`. */
@Serializable
data class MelodyNote(
    val interval: Interval?,
    val beats: Beats,
)

/** A pitch counted in [semitones] from the tonic of the song's key in the octave from middle C. */
@JvmInline @Serializable
value class Interval(val semitones: Int) {
    /** The MIDI number of this interval in [key], where middle C is 60. */
    fun toMidi(key: Key) = MIDDLE_C_MIDI + key.tonic.value + semitones

    companion object {
        /** The interval of the MIDI number [midi] in [key]. */
        fun ofMidi(midi: Int, key: Key) = Interval(midi - MIDDLE_C_MIDI - key.tonic.value)
    }
}

private const val MIDDLE_C_MIDI = 60

// Lyrics

@Serializable
data class LyricTrack(
    override val sections: List<Stanza>,
    override val arrangements: List<Arrangement>,
    override val capo: Int? = null,
    override val name: String? = null,
) : Track

/** The [notes] of a lyric section, led by a [pickup] that sounds at the end of the measure before it. */
@Serializable
data class Stanza(
    override val sectionId: SectionId,
    val notes: List<LyricNote>,
    val pickup: List<LyricNote> = emptyList(),
    val measuresPerLine: Int = 2,
) : Section {
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
    override val sections: List<Groove>,
    override val arrangements: List<Arrangement>,
    override val capo: Int? = null,
    override val name: String? = null,
) : Track

/**
 * The [lanes] of a drum section, one per drum, sounding together. [slotsPerMeasure] and [measuresPerLine] are the
 * layout of its tab.
 */
@Serializable
data class Groove(
    override val sectionId: SectionId,
    val lanes: List<DrumLane>,
    val slotsPerMeasure: Int = 16,
    val measuresPerLine: Int = 2,
) : Section {
    override val length get() = Beats(lanes.maxOfOrNull { lane -> lane.beats.sumOf { it.beats.ticks } } ?: 0)
}

/** The [beats] one [drum] plays in a groove. */
@Serializable
data class DrumLane(
    val drum: Drum,
    val beats: List<DrumBeat>,
)

/** A [hit] that lasts for [beats] until the next, or a rest when [hit] is `null`. */
@Serializable
data class DrumBeat(
    val hit: DrumHit?,
    val beats: Beats,
)

/** How a drum is struck. */
enum class DrumHit {
    Normal,
    Accent,
    Ghost,
}

enum class Drum {
    Kick,
    Snare,
    HiHat,
    Crash,
    Ride,
    Tom,
}

/**
 * The sections [sectionId] stands for in this track, an arrangement resolved into the sections it holds in order, or
 * `null` when the track does not define it or its arrangement holds a cycle.
 */
fun Track.sectionsOf(sectionId: SectionId): List<Section>? = sectionsOf(sectionId, emptySet())

private fun Track.sectionsOf(sectionId: SectionId, visited: Set<SectionId>): List<Section>? {
    if (sectionId in visited) return null
    sections.firstOrNull { it.sectionId == sectionId }?.let { return listOf(it) }
    val arrangement = arrangements.firstOrNull { it.sectionId == sectionId } ?: return null
    return arrangement.sections.flatMap { sectionsOf(it, visited + sectionId) ?: return null }
}

/** The length of [sectionId] in this track, or `null` when the track does not define it. */
fun Track.lengthOf(sectionId: SectionId) =
    sectionsOf(sectionId)?.let { sections -> Beats(sections.sumOf { it.length.ticks }) }

/**
 * The times this track plays [sectionId] to fill [span], the length the section has in the song: as many whole times
 * as fit, and at least once.
 */
fun Track.repeatsOf(sectionId: SectionId, span: Beats): Int {
    val length = lengthOf(sectionId)?.ticks?.takeIf { it > 0 } ?: return 1
    return (span.ticks / length).coerceAtLeast(1)
}

/** The length of [sectionId] in a song of these tracks: the longest track that defines it, or `null` for none. */
fun List<Track>.lengthOf(sectionId: SectionId) = mapNotNull { it.lengthOf(sectionId) }.maxByOrNull { it.ticks }

/**
 * The sections a song of these tracks plays in order: the [Music.arrangement] of [music], or each section of the
 * tracks once, in the order they first appear, when the arrangement is empty.
 */
fun List<Track>.sectionOrderOf(music: Music) =
    music.arrangement.ifEmpty { flatMap { track -> track.sections.map { it.sectionId } }.distinct() }
