package streetlight.model.data

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.uuid.Uuid

@Serializable
data class Music(
    val musicId: MusicId,
    val songId: SongId,
    val key: Key,
    val time: TimeSignature,
)

@JvmInline @Serializable
value class MusicId(override val value: Uuid): RecordId {
    companion object { fun random() = MusicId(Uuid.random()) }
}

@Serializable
data class Key(
    val tonic: Chroma,
    val mode: Mode,
)

enum class Mode {
    Major,
    Minor,
    Dorian,
    Mixolydian,
    Phrygian,
    Lydian,
    Locrian,
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
)

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