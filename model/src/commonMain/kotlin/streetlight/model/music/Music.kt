package streetlight.model.music

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.uuid.Uuid
import streetlight.model.data.RecordId

/**
 * A musical understanding of the song [songId], shown under its [name]: its key, time and tempo, the [sections] its
 * tracks hold content for, and the [arrangement] of them every track follows. A song has many, each one way of playing
 * it.
 *
 * The key and every track are written as played, and sound [capo] semitones higher, as chord shapes played behind a
 * guitar capo. A track's own [Track.capo] adds to it. The [bpm] counts the [TimeSignature.pulse].
 */
@Serializable
data class Music(
    val musicId: MusicId,
    val songId: SongId,
    val name: String,
    val key: Key,
    val time: TimeSignature,
    val bpm: Int = 90,
    val capo: Int? = null,
    val sections: List<MusicSection> = emptyList(),
    val arrangement: List<SectionId> = emptyList(),
) {
    /** The name of the section [sectionId], or a placeholder when the music holds no such section. */
    fun nameOf(sectionId: SectionId) = sections.firstOrNull { it.sectionId == sectionId }?.name ?: UNNAMED_SECTION

    /** The section named [name], or `null` for none. */
    fun sectionOf(name: String) = sections.firstOrNull { it.name == name }
}

/** A section of a music, known by a [name] no other section of the music holds. */
@Serializable
data class MusicSection(
    val sectionId: SectionId,
    val name: String,
)

/** The name shown for a section its music does not hold. */
const val UNNAMED_SECTION = "?"

@JvmInline @Serializable
value class MusicId(override val value: Uuid): RecordId {
    override fun toString() = value.toString()
    companion object { fun random() = MusicId(Uuid.random()) }
}

@Serializable
data class Key(
    val tonic: Chroma,
    val mode: Mode,
) {
    /** The tonic of the major scale the mode is drawn from, which scale degrees count from. */
    val majorTonic get() = Chroma.of(tonic.value - mode.semitonesFromMajor)
}

/** A mode, by the [semitonesFromMajor] its tonic sits above the tonic of the major scale it is drawn from. */
enum class Mode(val semitonesFromMajor: Int) {
    Major(0),
    Minor(9),
    Dorian(2),
    Mixolydian(7),
    Phrygian(4),
    Lydian(5),
    Locrian(11),
}

@Serializable @JvmInline
value class Chroma private constructor(val value: Int) {
    companion object {
        fun of(semitones: Int) = Chroma(semitones.mod(12))
    }
}

@Serializable
data class TimeSignature(
    val beats: Int,
    val beatUnit: Int,
) {
    /** The length of one beat of the signature, counting a quarter note as one of [Beats]. */
    val beat get() = Beats.of(4, beatUnit)

    /**
     * The length of the beat a tempo counts: a dotted [beat] in a compound meter such as 6/8, where [beats] is a
     * multiple of 3 above 3, and [beat] otherwise.
     */
    val pulse get() = if (beats > 3 && beats % 3 == 0) Beats.of(12, beatUnit) else beat

    /** The length of a measure, counting a quarter note as one of [Beats]. */
    val measure get() = Beats.of(beats * 4, beatUnit)
}

@JvmInline @Serializable
value class Beats(val ticks: Int) : Comparable<Beats> {
    operator fun plus(other: Beats) = Beats(ticks + other.ticks)
    operator fun minus(other: Beats) = Beats(ticks - other.ticks)
    operator fun times(n: Int) = Beats(ticks * n)
    override fun compareTo(other: Beats) = ticks.compareTo(other.ticks)

    companion object {
        const val TICKS_PER_BEAT = 960

        fun of(whole: Int) = Beats(whole * TICKS_PER_BEAT)
        fun of(numerator: Int, denominator: Int) = Beats(numerator * TICKS_PER_BEAT / denominator)
    }
}