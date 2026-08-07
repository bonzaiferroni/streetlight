package kampfire.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.serialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure

@Serializable
sealed interface Outcome <out T> {
    val message: String?

    val isOk get() = when(this) {
        is Ok<T> -> true
        else -> false
    }

    fun toDataOrNull(onProblem: ((Problem) -> Unit)? = null): T? = when (this) {
        is Ok -> data
        is Problem -> {
            onProblem?.invoke(this)
            null
        }
    }
}

inline fun <T> Outcome<T>.toDataOr(onProblem: (Problem) -> Nothing): T = when (this) {
    is Ok -> data
    is Problem -> onProblem(this)
}

inline fun <T> Outcome<T>.toDataOr(onProblem: (Problem) -> Unit, onFinished: () -> Nothing): T = when (this) {
    is Ok -> data
    is Problem -> {
        onProblem(this)
        onFinished()
    }
}

@Serializable
data class Ok<T>(
    val data: T,
    override val message: String? = null
): Outcome<T>

@Serializable
data class Problem(
    override val message: String,
): Outcome<Nothing>

fun <T> outcomeOf(data: T?): Outcome<T>? = when (data) {
    null -> null
    else -> Ok(data)
}

fun <T> T?.toOutcome() = outcomeOf(this)

fun <T> T.toOk() = Ok(this)

class OutcomeSerializer<T>(
    private val dataSerializer: KSerializer<T>
) : KSerializer<Outcome<T>> {

    @Suppress("UNCHECKED_CAST")
    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor("Outcome") {
            element("message", serialDescriptor<String?>(), isOptional = true)
            element("data", dataSerializer.descriptor, isOptional = true)
        }

    override fun serialize(encoder: Encoder, value: Outcome<T>) {
        encoder.encodeStructure(descriptor) {
            value.message?.let { encodeStringElement(descriptor, 0, it) }
            if (value is Ok) encodeSerializableElement(descriptor, 1, dataSerializer, value.data)
        }
    }

    override fun deserialize(decoder: Decoder): Outcome<T> {
        var message: String? = null
        var data: T? = null
        var dataSet = false

        decoder.decodeStructure(descriptor) {
            while (true) {
                when (val index = decodeElementIndex(descriptor)) {
                    0 -> message = decodeStringElement(descriptor, 0)
                    1 -> {
                        data = decodeSerializableElement(descriptor, 1, dataSerializer)
                        dataSet = true
                    }
                    CompositeDecoder.DECODE_DONE -> break
                    else -> error("Unexpected index: $index")
                }
            }
        }

        return if (dataSet) {
            @Suppress("UNCHECKED_CAST")
            Ok(data = data as T, message = message)
        } else {
            Problem(message = message ?: "Expected data was null")
        }
    }
}

