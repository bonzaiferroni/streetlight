package streetlight.model.data

import kotlinx.serialization.Serializable

/** A sequence a song part plays, repeated [repetitions] times. */
@Serializable
sealed interface PartSequence {
    val sequenceId: SequenceId
    val repetitions: Int
    val measureBeats: Int?
    fun getMeasureCount(songMeasureBeats: Int): Int
    fun setSequenceId(sequenceId: SequenceId): PartSequence
    fun setRepetitions(repetitions: Int): PartSequence
    fun setMeasureBeats(measureBeats: Int?): PartSequence

    /** The sequence id, with its repetitions when there are more than one. */
    fun toLabel() = repetitions.takeIf { it > 1 }?.let { "$sequenceId (${it}x)" } ?: sequenceId
}

/** The id of a [PartSequence] within its song. */
typealias SequenceId = String