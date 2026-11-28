package streetlight.model.music

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

/** A melody track, sung with the lyric track [lyricsId], or the first of its music when `null`. */
@Serializable
data class MelodyTrack(
    override val trackId: TrackId,
    override val musicId: MusicId,
    override val name: String,
    override val notations: List<Phrase>,
    override val arrangements: List<Arrangement>,
    override val sections: Map<SectionId, NotationId>,
    override val capo: Int? = null,
    val lyricsId: TrackId? = null,
) : Track {
    override val trackType get() = TrackType.Melody
}

/** The [notes] of a melody notation, those before its [downbeat] a pickup that sounds at the end of the measure before it. */
@Serializable
data class Phrase(
    override val notationId: NotationId,
    override val name: String?,
    val notes: List<MelodyNote>,
    override val downbeat: Beats = Beats(0),
    override val measuresPerLine: Int = 4,
) : Notation {
    override val length get() = Beats(notes.sumOf { it.beats.ticks } - downbeat.ticks)
    override val trackType get() = TrackType.Melody
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
