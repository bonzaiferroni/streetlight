package streetlight.model.data

import kotlinx.serialization.Serializable

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

enum class Instrument(val label: String, val midiProgram: Int, val notationLabel: String) {
    RhythmGuitar("Rhythm Guitar", 24, "Chords"),
    Vocals("Vocals", 52, "Lyrics");

    fun createSequence(sequenceId: SequenceId): PartSequence = when (this) {
        RhythmGuitar -> ChordSequence.Empty.copy(sequenceId = sequenceId)
        Vocals -> VocalSequence.Empty.copy(sequenceId = sequenceId)
    }
}