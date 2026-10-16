package streetlight.model.data

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.uuid.Uuid

@JvmInline
@Serializable
value class CustomerId(override val value: Uuid): RecordId {
    override fun toString() = value.toString()

    companion object {
        fun random() = CustomerId(Uuid.random())
    }
}
