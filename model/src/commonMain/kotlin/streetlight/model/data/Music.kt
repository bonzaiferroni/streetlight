package streetlight.model.data

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.uuid.Uuid

/** A musical understanding of a song: its key, time and tempo, and the [arrangement] of sections every track follows. */
@Serializable
data class Music(
    val musicId: MusicId,
    val songId: SongId,
    val key: Key,
    val time: TimeSignature,
    val bpm: Int = 90,
    val arrangement: List<SectionId> = emptyList(),
)

@JvmInline @Serializable
value class MusicId(override val value: Uuid): RecordId {
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