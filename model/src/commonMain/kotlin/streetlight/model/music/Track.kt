package streetlight.model.music

import koala.SvgFile
import koala.SvgPack
import koala.modifier.Palette
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.uuid.Uuid

/**
 * A part of the music [musicId] played by a voice or instrument, known by its [trackId] and shown under its [name],
 * unique within its music: its [notations], its [arrangements] of notations into larger ones, and the notation or
 * arrangement it plays for each of the music's [sections].
 *
 * The track sounds [capo] semitones higher than written, or lower for a negative one, on top of the [Music.capo] of
 * its song.
 */
@Serializable
sealed interface Track {
    val trackId: TrackId
    val musicId: MusicId
    val name: String
    val trackType: TrackType
    val notations: List<Notation>
    val arrangements: List<Arrangement>
    val sections: Map<SectionId, NotationId>
    val capo: Int?
}

/** The types of [Track], each with the [svg] and [color] that stand for it. */
enum class TrackType(val svg: SvgPack, val color: Palette) {
    Chords(SvgFile.AcousticGuitar, Palette.Gold),
    Melody(SvgFile.Music, Palette.Cyan),
    Lyrics(SvgFile.Microphone, Palette.Magenta),
    Drums(SvgFile.Drum, Palette.SeaGreen),
}

/** The identity of a track, which it keeps through a change of its name. */
@JvmInline @Serializable
value class TrackId(val value: Uuid) {
    override fun toString() = value.toString()
    companion object { fun random() = TrackId(Uuid.random()) }
}

/** The track name [base], or [base] with the first number after it that [held] leaves free, as `Melody 2`. */
fun freeTrackNameOf(base: String, held: Set<String>) = generateSequence(1) { it + 1 }
    .map { if (it == 1) base else "$base $it" }
    .first { it !in held }

/** The notes of a part of a track, [length] long, shown as [name] and written [measuresPerLine] measures to a line. */
@Serializable
sealed interface Notation {
    val notationId: NotationId
    val name: String?
    val length: Beats
    val measuresPerLine: Int
}

/** The identity of a notation or an arrangement of a track. */
@JvmInline @Serializable
value class NotationId(val value: Uuid) {
    override fun toString() = value.toString()
    companion object { fun random() = NotationId(Uuid.random()) }
}

/** The [notations] played in order as [notationId], shown as [name], each a notation or another arrangement. */
@Serializable
data class Arrangement(
    val notationId: NotationId,
    val name: String,
    val notations: List<NotationId>,
)

/** This track named [name], with [arrangements], [sections] and [capo], and the notations it has. */
fun Track.copyTrack(
    name: String = this.name,
    arrangements: List<Arrangement> = this.arrangements,
    sections: Map<SectionId, NotationId> = this.sections,
    capo: Int? = this.capo,
): Track = when (this) {
    is ChordTrack -> copy(name = name, arrangements = arrangements, sections = sections, capo = capo)
    is MelodyTrack -> copy(name = name, arrangements = arrangements, sections = sections, capo = capo)
    is LyricTrack -> copy(name = name, arrangements = arrangements, sections = sections, capo = capo)
    is DrumTrack -> copy(name = name, arrangements = arrangements, sections = sections, capo = capo)
}

/**
 * This track with [notation] in place of its notation of the same id, or after its notations when it has none. A
 * notation of another track's type leaves the track as it is.
 */
fun Track.withNotation(notation: Notation): Track = when (this) {
    is ChordTrack -> (notation as? Progression)?.let { copy(notations = notations.withNotation(it)) }
    is MelodyTrack -> (notation as? Phrase)?.let { copy(notations = notations.withNotation(it)) }
    is LyricTrack -> (notation as? Stanza)?.let { copy(notations = notations.withNotation(it)) }
    is DrumTrack -> (notation as? Groove)?.let { copy(notations = notations.withNotation(it)) }
} ?: this

private fun <T : Notation> List<T>.withNotation(notation: T) =
    if (any { it.notationId == notation.notationId }) map { if (it.notationId == notation.notationId) notation else it }
    else this + notation

/** This track without its notation or arrangement [notationId], and without the sections it played. */
fun Track.withoutNotation(notationId: NotationId): Track = when (this) {
    is ChordTrack -> copy(notations = notations.filter { it.notationId != notationId })
    is MelodyTrack -> copy(notations = notations.filter { it.notationId != notationId })
    is LyricTrack -> copy(notations = notations.filter { it.notationId != notationId })
    is DrumTrack -> copy(notations = notations.filter { it.notationId != notationId })
}.copyTrack(
    arrangements = arrangements.filter { it.notationId != notationId },
    sections = sections.filterValues { it != notationId },
)

/** An empty track of [trackType] in the music [musicId], named [name]. */
fun emptyTrackOf(trackType: TrackType, musicId: MusicId, name: String): Track {
    val trackId = TrackId.random()
    return when (trackType) {
        TrackType.Chords -> ChordTrack(trackId, musicId, name, emptyList(), emptyList(), emptyMap())
        TrackType.Melody -> MelodyTrack(trackId, musicId, name, emptyList(), emptyList(), emptyMap())
        TrackType.Lyrics -> LyricTrack(trackId, musicId, name, emptyList(), emptyList(), emptyMap())
        TrackType.Drums -> DrumTrack(trackId, musicId, name, emptyList(), emptyList(), emptyMap())
    }
}

/** A notation [notationId] of this track's type named [name], holding nothing. */
fun Track.emptyNotationOf(notationId: NotationId, name: String?): Notation = when (this) {
    is ChordTrack -> Progression(notationId, name, emptyList())
    is MelodyTrack -> Phrase(notationId, name, emptyList())
    is LyricTrack -> Stanza(notationId, name, emptyList())
    is DrumTrack -> Groove(notationId, name, emptyList())
}

/**
 * The name of the notation or arrangement [notationId], or the name of [music] and the track when it has none, as
 * `Amazing Grace Chords`.
 */
fun Track.nameOf(notationId: NotationId, music: Music) =
    notations.firstOrNull { it.notationId == notationId }?.let { it.name ?: "${music.name} $name" }
        ?: arrangements.firstOrNull { it.notationId == notationId }?.name
        ?: UNNAMED_SECTION

/** The sections of the music this track plays [notationId] for, in the order it states them. */
fun Track.sectionIdsOf(notationId: NotationId) = sections.filterValues { it == notationId }.keys.toList()

/**
 * The notations [notationId] stands for in this track, an arrangement resolved into the notations it holds in order,
 * or `null` when the track does not define it or its arrangement holds a cycle.
 */
fun Track.notationsOf(notationId: NotationId): List<Notation>? = notationsOf(notationId, emptySet())

private fun Track.notationsOf(notationId: NotationId, visited: Set<NotationId>): List<Notation>? {
    if (notationId in visited) return null
    notations.firstOrNull { it.notationId == notationId }?.let { return listOf(it) }
    val arrangement = arrangements.firstOrNull { it.notationId == notationId } ?: return null
    return arrangement.notations.flatMap { notationsOf(it, visited + notationId) ?: return null }
}

/** The length of [notationId] in this track, or `null` when the track does not define it. */
fun Track.lengthOf(notationId: NotationId) =
    notationsOf(notationId)?.let { notations -> Beats(notations.sumOf { it.length.ticks }) }

/**
 * The times this track plays [notationId] to fill [span], the length of the section it plays it for: as many whole
 * times as fit, and at least once.
 */
fun Track.repeatsOf(notationId: NotationId, span: Beats): Int {
    val length = lengthOf(notationId)?.ticks?.takeIf { it > 0 } ?: return 1
    return (span.ticks / length).coerceAtLeast(1)
}
