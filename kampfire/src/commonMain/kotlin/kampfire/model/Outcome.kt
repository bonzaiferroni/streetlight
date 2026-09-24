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
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/** The result of a call that can fail: [Ok] with data, or a [Problem]. Either may carry a [message] for the user. */
@Serializable
sealed interface Outcome <out T> {
    val message: String?

    val isOk get() = when(this) {
        is Ok<T> -> true
        else -> false
    }
}

/** The data of an [Ok], or the result of [onProblem], which must leave the caller. */
@OptIn(ExperimentalContracts::class)
inline fun <T> Outcome<T>.toDataOr(onProblem: (Problem) -> Nothing): T {
    contract {
        callsInPlace(onProblem, InvocationKind.AT_MOST_ONCE)
    }
    return when (this) {
        is Ok -> data
        is Problem -> onProblem(this)
    }
}

/**
 * The data of an [Ok], delivering the outcome's messages along the way.
 *
 * On [Ok] it delivers the outcome's message, or [defaultOkMessage], to [okMessenger]. On [Problem] it delivers
 * the problem to [messenger] and calls [onProblem], which must leave the caller.
 */
@OptIn(ExperimentalContracts::class)
inline fun <T> Outcome<T>.toDataOr(
    messenger: Messenger,
    defaultOkMessage: String? = null,
    okMessenger: Messenger = messenger,
    onProblem: (Problem) -> Nothing
): T {
    contract {
        callsInPlace(onProblem, InvocationKind.AT_MOST_ONCE)
    }
    return when (this) {
        is Ok -> {
            val deliveredMessage = message ?: defaultOkMessage
            deliveredMessage?.let {
                okMessenger.deliverSuccess(it)
            }
            data
        }
        is Problem -> {
            messenger.deliver(this)
            onProblem(this)
        }
    }
}

/** The data of an [Ok], or `null` after passing the problem to [onProblem]. */
fun <T> Outcome<T>.toDataOrNull(onProblem: ((Problem) -> Unit)? = null): T? = when (this) {
    is Ok -> data
    is Problem -> {
        onProblem?.invoke(this)
        null
    }
}

/** The data of an [Ok], or `null`, delivering the outcome's messages as [toDataOr] does. */
fun <T> Outcome<T>.toDataOrNull(
    messenger: Messenger,
    defaultOkMessage: String? = null,
    okMessenger: Messenger = messenger,
) = toDataOrNull(messenger, defaultOkMessage, okMessenger) { it }

/** The data of an [Ok] mapped with [block], or `null`, delivering the outcome's messages as [toDataOr] does. */
fun <T1, T2> Outcome<T1>.toDataOrNull(
    messenger: Messenger,
    defaultOkMessage: String? = null,
    okMessenger: Messenger = messenger,
    block: (T1) -> T2
): T2? = when (this) {
    is Ok -> {
        val deliveredMessage = message ?: defaultOkMessage
        deliveredMessage?.let {
            okMessenger.deliver(UIMessage(it, UIMessageType.Success))
        }
        block(data)
    }

    is Problem -> {
        messenger.deliver(this)
        null
    }
}

/** The data of an [Ok], or, on a problem, [onProblem] followed by [onFinished], which must leave the caller. */
inline fun <T> Outcome<T>.toDataOr(onProblem: (Problem) -> Unit, onFinished: () -> Nothing): T = when (this) {
    is Ok -> data
    is Problem -> {
        onProblem(this)
        onFinished()
    }
}

/** A successful [Outcome] holding [data]. */
@Serializable
data class Ok<T>(
    val data: T,
    override val message: String? = null
): Outcome<T>

/**
 * A failed [Outcome]. Its [message] is written for the user.
 *
 * A problem is declared once, as a property of a `FooProblem` object, and returned from there.
 */
@Serializable
data class  Problem(
    override val message: String,
): Outcome<Nothing>

/** [Ok] holding [data], or `null` when there is none. */
fun <T> outcomeOf(data: T?): Outcome<T>? = when (data) {
    null -> null
    else -> Ok(data)
}

/** [Ok] holding this value, or `null` when it is `null`. */
fun <T> T?.toOutcome() = outcomeOf(this)

/** [Ok] holding this value. */
fun <T> T.toOk() = Ok(this)

/**
 * Serializes an [Outcome] as an optional message and optional data. An outcome with data reads back as [Ok], and
 * one without as [Problem].
 */
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

