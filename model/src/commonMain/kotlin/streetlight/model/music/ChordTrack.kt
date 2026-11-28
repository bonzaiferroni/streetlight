package streetlight.model.music

import kotlinx.serialization.Serializable

@Serializable
data class ChordTrack(
    override val trackId: TrackId,
    override val musicId: MusicId,
    override val name: String,
    override val notations: List<Progression>,
    override val arrangements: List<Arrangement>,
    override val sections: Map<SectionId, NotationId>,
    override val capo: Int? = null,
) : Track {
    override val trackType get() = TrackType.Chords
}

/** The [notes] of a chord notation, those before its [downbeat] a pickup that sounds at the end of the measure before it. */
@Serializable
data class Progression(
    override val notationId: NotationId,
    override val name: String?,
    val notes: List<ChordNote>,
    override val downbeat: Beats = Beats(0),
    override val measuresPerLine: Int = 4,
) : Notation {
    override val length get() = Beats(notes.sumOf { it.beats.ticks } - downbeat.ticks)
    override val trackType get() = TrackType.Chords
}

/** A [chord] held for [beats], or a rest when [chord] is `null`. */
@Serializable
data class ChordNote(
    val chord: Chord?,
    val beats: Beats,
)
