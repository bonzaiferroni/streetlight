package streetlight.model.data

import kotlinx.serialization.Serializable

/** A part of a song played by one instrument, with its sequences. */
@Serializable
data class SongPart(
    val instrument: Instrument,
    val style: NotationStyle = NotationStyle.Letters,
    val midiProgram: Int? = null,
    val sequences: List<PartSequence> = emptyList(),
) {
    companion object {
        fun createEmpty(instrument: Instrument) = SongPart(
            instrument = instrument,
            sequences = listOf(instrument.createSequence("Verse")),
        )
    }
}

/** The instruments a song part can be played on, with their MIDI program. */
enum class Instrument(val label: String, val midiProgram: Int?, val notationLabel: String) {
    RhythmGuitar("Rhythm Guitar", 24, "Chords"),
    Vocals("Vocals", 52, "Lyrics"),
    Drums("Drums", null, "Loop");

    /** An empty sequence of the kind this instrument plays. */
    fun createSequence(sequenceId: SequenceId): PartSequence = when (this) {
        RhythmGuitar -> ChordSequence.Empty.copy(sequenceId = sequenceId)
        Vocals -> VocalSequence.Empty.copy(sequenceId = sequenceId)
        Drums -> DrumSequence.Empty.copy(sequenceId = sequenceId)
    }
}