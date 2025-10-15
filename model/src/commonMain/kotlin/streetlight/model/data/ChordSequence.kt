package streetlight.model.data

import kotlinx.serialization.Serializable

@Serializable
data class ChordSequence(
    override val sequenceId: SequenceId,
    override val repetitions: Int = 1,
    override val measureBeats: Int? = null,
    val chords: List<MeasureChord>,
): PartSequence {

    override fun getMeasureCount(songMeasureBeats: Int): Int {
        val measureBeats = measureBeats ?: songMeasureBeats
        return chords.sumOf { it.duration ?: measureBeats } / measureBeats
    }

    fun getChordAt(index: Int) = chords[index % chords.size]

    override fun setSequenceId(sequenceId: SequenceId) = copy(sequenceId = sequenceId)
    override fun setRepetitions(repetitions: Int) = copy(repetitions = repetitions)
    override fun setMeasureBeats(measureBeats: Int?) = copy(measureBeats = measureBeats)

    companion object {
        val Empty get() = ChordSequence(
            sequenceId = "Verse",
            chords = emptyList()
        )
    }
}