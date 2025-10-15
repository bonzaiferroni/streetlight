package streetlight.model.data

import kotlinx.serialization.Serializable

@Serializable
data class VocalSequence(
    override val sequenceId: SequenceId,
    override val repetitions: Int = 1,
    override val measureBeats: Int?,
    val measureCount: Int,
    val lyrics: String,
): PartSequence {
    override fun getMeasureCount(songMeasureBeats: Int) = measureCount

    override fun setSequenceId(sequenceId: SequenceId) = copy(sequenceId = sequenceId)
    override fun setRepetitions(repetitions: Int) = copy(repetitions = repetitions)
    override fun setMeasureBeats(measureBeats: Int?) = copy(measureBeats = measureBeats)

    companion object {
        val Empty get() = VocalSequence(
            sequenceId = "Verse",
            repetitions = 1,
            measureBeats = null,
            measureCount = 8,
            lyrics = "",
        )
    }
}