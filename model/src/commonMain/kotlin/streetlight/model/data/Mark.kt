package streetlight.model.data

import kampfire.model.Labeled
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.uuid.Uuid

@Serializable
data class Mark(
    val markId: MarkId,
    val lean: Lean,
    val name: String
) {
    companion object {
        val ValidLength = IntRange(2, 32)
    }
}

@JvmInline @Serializable
value class MarkId(override val value: Uuid): RecordId {
    override fun toString() = value.toString()
}

enum class Lean(override val label: String, val value: Int): Labeled {
    StrongNegative("-2", -2),
    Negative("-1", -1),
    Neutral("0", 0),
    Positive("+1", +1),
    StrongPositive("+2", +2),
}