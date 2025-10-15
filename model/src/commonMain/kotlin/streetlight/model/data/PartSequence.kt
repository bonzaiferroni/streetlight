package streetlight.model.data

import kotlinx.serialization.Serializable

@Serializable
sealed interface PartSequence {
    val sequenceId: SequenceId
    val repetitions: Int
    val measureBeats: Int?
    fun getMeasureCount(songMeasureBeats: Int): Int
    fun setSequenceId(sequenceId: SequenceId): PartSequence
    fun setRepetitions(repetitions: Int): PartSequence
    fun setMeasureBeats(measureBeats: Int?): PartSequence

    fun toLabel() = repetitions.takeIf { it > 1 }?.let { "$sequenceId (${it}x)" } ?: sequenceId
}

typealias SequenceId = String