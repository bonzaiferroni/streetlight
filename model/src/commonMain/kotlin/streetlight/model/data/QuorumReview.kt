package streetlight.model.data

import kotlinx.serialization.Serializable

@Serializable
sealed interface QuorumReview

@Serializable
data class EditReview(
    val quorum: Quorum,
    val review: Review,
    val editLog: EditLog
): QuorumReview