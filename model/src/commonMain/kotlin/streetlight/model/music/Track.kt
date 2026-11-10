package streetlight.model.music

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.uuid.Uuid

/**
 * A part played by a voice or instrument, named by its [trackId], unique within its song: its [sections], and its
 * [arrangements] of sections into larger ones.
 *
 * The track sounds [capo] semitones higher than written, or lower for a negative one, on top of the [Music.capo] of
 * its song.
 */
@Serializable
sealed interface Track {
    val trackId: TrackId
    val sections: List<Section>
    val arrangements: List<Arrangement>
    val capo: Int?
}

/** The name of a track, unique within its song. */
@JvmInline @Serializable
value class TrackId(val name: String)

/** The track id [base], or [base] with the first number after it that [held] leaves free, as `Melody 2`. */
fun freeTrackIdOf(base: String, held: Set<TrackId>) = generateSequence(1) { it + 1 }
    .map { TrackId(if (it == 1) base else "$base $it") }
    .first { it !in held }

/** A section of a track, [length] long, written [measuresPerLine] measures to a line of its text. */
@Serializable
sealed interface Section {
    val sectionId: SectionId
    val length: Beats
    val measuresPerLine: Int
}

/** The identity of a section of a music, which every track shares. Its name is kept by [Music.sections]. */
@JvmInline @Serializable
value class SectionId(val value: Uuid) {
    companion object { fun random() = SectionId(Uuid.random()) }
}

/** A section made of other [sections] played in order, each a section or another arrangement. */
@Serializable
data class Arrangement(
    val sectionId: SectionId,
    val sections: List<SectionId>,
)

// Chords

@Serializable
data class ChordTrack(
    override val trackId: TrackId,
    override val sections: List<Progression>,
    override val arrangements: List<Arrangement>,
    override val capo: Int? = null,
) : Track

/** The [notes] of a chord section, led by a [pickup] that sounds at the end of the measure before it. */
@Serializable
data class Progression(
    override val sectionId: SectionId,
    val notes: List<ChordNote>,
    val pickup: List<ChordNote> = emptyList(),
    override val measuresPerLine: Int = 4,
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
    override val trackId: TrackId,
    override val sections: List<Phrase>,
    override val arrangements: List<Arrangement>,
    override val capo: Int? = null,
) : Track

/**
 * The [notes] of a melody section, led by a [pickup] that sounds at the end of the measure before it. Its [lyrics] name
 * the stanzas and arrangements of stanzas that may be sung to it, `null` for any.
 */
@Serializable
data class Phrase(
    override val sectionId: SectionId,
    val notes: List<MelodyNote>,
    val pickup: List<MelodyNote> = emptyList(),
    override val measuresPerLine: Int = 4,
    val lyrics: List<SectionId>? = null,
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
    override val trackId: TrackId,
    override val sections: List<Stanza>,
    override val arrangements: List<Arrangement>,
    override val capo: Int? = null,
) : Track

/** The [notes] of a lyric section, led by a [pickup] that sounds at the end of the measure before it. */
@Serializable
data class Stanza(
    override val sectionId: SectionId,
    val notes: List<LyricNote>,
    val pickup: List<LyricNote> = emptyList(),
    override val measuresPerLine: Int = 2,
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
    override val trackId: TrackId,
    override val sections: List<Groove>,
    override val arrangements: List<Arrangement>,
    override val capo: Int? = null,
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
    override val measuresPerLine: Int = 2,
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

/** This track named [trackId], with [arrangements] and [capo], and the sections it has. */
fun Track.copyTrack(
    trackId: TrackId = this.trackId,
    arrangements: List<Arrangement> = this.arrangements,
    capo: Int? = this.capo,
): Track = when (this) {
    is ChordTrack -> copy(trackId = trackId, arrangements = arrangements, capo = capo)
    is MelodyTrack -> copy(trackId = trackId, arrangements = arrangements, capo = capo)
    is LyricTrack -> copy(trackId = trackId, arrangements = arrangements, capo = capo)
    is DrumTrack -> copy(trackId = trackId, arrangements = arrangements, capo = capo)
}

/**
 * This track with [section] in place of its section of the same id, or after its sections when it has none. A section
 * of another track's type leaves the track as it is.
 */
fun Track.withSection(section: Section): Track = when (this) {
    is ChordTrack -> (section as? Progression)?.let { copy(sections = sections.withSection(it)) }
    is MelodyTrack -> (section as? Phrase)?.let { copy(sections = sections.withSection(it)) }
    is LyricTrack -> (section as? Stanza)?.let { copy(sections = sections.withSection(it)) }
    is DrumTrack -> (section as? Groove)?.let { copy(sections = sections.withSection(it)) }
} ?: this

private fun <T : Section> List<T>.withSection(section: T) =
    if (any { it.sectionId == section.sectionId }) map { if (it.sectionId == section.sectionId) section else it }
    else this + section

/** This track without its section or arrangement [sectionId]. */
fun Track.withoutSection(sectionId: SectionId): Track = when (this) {
    is ChordTrack -> copy(sections = sections.filter { it.sectionId != sectionId })
    is MelodyTrack -> copy(sections = sections.filter { it.sectionId != sectionId })
    is LyricTrack -> copy(sections = sections.filter { it.sectionId != sectionId })
    is DrumTrack -> copy(sections = sections.filter { it.sectionId != sectionId })
}.copyTrack(arrangements = arrangements.filter { it.sectionId != sectionId })

/** A section [sectionId] of this track's type, holding nothing. */
fun Track.emptySectionOf(sectionId: SectionId): Section = when (this) {
    is ChordTrack -> Progression(sectionId, emptyList())
    is MelodyTrack -> Phrase(sectionId, emptyList())
    is LyricTrack -> Stanza(sectionId, emptyList())
    is DrumTrack -> Groove(sectionId, emptyList())
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
