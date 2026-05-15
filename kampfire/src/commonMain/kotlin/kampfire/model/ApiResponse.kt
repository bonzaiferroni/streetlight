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

// must use ApiResponseSerializer because of generic argument
sealed interface ApiResponse <T> {
    val message: String?
    val data: T?
}

@Serializable
data class Ok<T>(
    override val data: T,
    override val message: String? = null
): ApiResponse<T>

// the purpose of this class is to communicate the issue to the user
@Serializable
data class Problem<T>(
    override val message: String,
): ApiResponse<T> {
    override val data: T? get() = null
}

fun <T> responseOf(data: T?): ApiResponse<T>? = when (data) {
    null -> null
    else -> Ok(data)
}

fun <T> T?.toResponse() = responseOf(this)

class ApiResponseSerializer<T>(
    private val dataSerializer: KSerializer<T>
) : KSerializer<ApiResponse<T>> {

    @Suppress("UNCHECKED_CAST")
    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor("ApiResponse") {
            element("message", serialDescriptor<String?>(), isOptional = true)
            element("data", dataSerializer.descriptor, isOptional = true)
        }

    override fun serialize(encoder: Encoder, value: ApiResponse<T>) {
        encoder.encodeStructure(descriptor) {
            value.message?.let { encodeStringElement(descriptor, 0, it) }
            value.data?.let { encodeSerializableElement(descriptor, 1, dataSerializer, it) }
        }
    }

    override fun deserialize(decoder: Decoder): ApiResponse<T> {
        var message: String? = null
        var data: T? = null

        decoder.decodeStructure(descriptor) {
            while (true) {
                when (val index = decodeElementIndex(descriptor)) {
                    0 -> message = decodeStringElement(descriptor, 0)
                    1 -> data = decodeSerializableElement(descriptor, 1, dataSerializer)
                    CompositeDecoder.DECODE_DONE -> break
                    else -> error("Unexpected index: $index")
                }
            }
        }

        return if (data != null) {
            @Suppress("UNCHECKED_CAST")
            Ok(data = data as T, message = message)
        } else {
            Problem(message = message ?: "Unknown error")
        }
    }
}

fun <T> ApiResponse<T>?.getDataOrNull() = when (this) {
    is Ok -> this.data
    is Problem -> null.also { println("Problem: ${this.message}") }
    null -> null.also { println("Response was null") }
}

fun <T> ApiResponse<T>?.handleResponse(
    onMessage: (String) -> Unit,
    okMessage: String? = null,
) = handleResponse(onMessage, okMessage) { it }

fun <T1, T2> ApiResponse<T1>?.handleResponse(
    onMessage: (String) -> Unit,
    okMessage: String? = null,
    block: (T1) -> T2
): T2? = when (this) {
    is Ok -> {
        okMessage?.let {
            onMessage(it)
        }
        block(data)
    }

    is Problem -> {
        onMessage(message)
        null
    }

    null -> {
        onMessage("No response.")
        null
    }
}

fun <T> ApiResponse<T>?.isOk() = when (this) {
    is Ok -> true
    else -> false
}