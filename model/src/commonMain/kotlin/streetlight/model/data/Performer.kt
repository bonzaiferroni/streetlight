package streetlight.model.data

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.uuid.Uuid

/** A user's profile as a street performer, with the handle their tips go to. */
@Serializable
data class Performer(
    val performerId: PerformerId,
    val starId: StarId,
    val venmo: String,
    val stageName: String,
)

@JvmInline @Serializable
value class PerformerId(override val value: Uuid): RecordId {
    companion object { fun random() = PerformerId(Uuid.random()) }
}
