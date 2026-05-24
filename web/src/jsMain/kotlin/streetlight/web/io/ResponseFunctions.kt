@file:OptIn(ExperimentalSerializationApi::class)

package streetlight.web.io

import kampfire.model.ApiResponse
import kampfire.model.ApiResponseSerializer
import kampfire.model.Ok
import kampfire.model.Problem
import koala.utils.jsonConfig
import kotlinx.coroutines.await
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.serializer
import org.khronos.webgl.Int8Array
import org.w3c.fetch.Response
import streetlight.model.data.RecordId
import streetlight.model.data.toRecordId

suspend inline fun <reified Returned> Response.tryDecode(encoding: EncodingType?): Returned? {
    return when (encoding) {
        EncodingType.Cbor -> tryDecodeBytes()
        EncodingType.Json, null -> tryDecodeText()
    }
}

suspend inline fun <reified Returned> Response.tryDecodeBytes(): Returned? {
    if (status.toInt() == 204 || !ok) return null
    val buffer = arrayBuffer().await()
    val bytes = Int8Array(buffer).unsafeCast<ByteArray>()
    return Cbor.decodeFromByteArray<Returned>(bytes)
}

suspend inline fun <reified Returned> Response.tryDecodeTextResponse(): ApiResponse<Returned>? {
    val status = status.toInt()
    return when (status) {
        404 -> Problem("Not found")
        200 -> when (val obj = tryDecodeText<Returned>()) {
            null -> Problem("Error decoding response.")
            else -> Ok(obj)
        }
        else -> Problem("Unhandled response error: $status")
    }
}

suspend inline fun <reified Returned> Response.tryDecodeText(): Returned? {
    if (!ok) {
        console.log("request failed: $status")
        return null
    }

    val text = text().await()

    return try {
        when (Returned::class) {
            String::class -> text as Returned

            Int::class -> text.toIntOrNull() as Returned?
            Long::class -> text.toLongOrNull() as Returned?

            Double::class -> text.toDoubleOrNull() as Returned?
            Float::class -> text.toFloatOrNull() as Returned?
            Boolean::class -> text.toBooleanStrictOrNull() as Returned?
            RecordId::class -> text.toRecordId<Returned>()

            else -> jsonConfig.decodeFromString<Returned>(text)
        }
    } catch (e: Exception) {
        console.log("failed to parse response:\n${e}\n${url}\ndata: ${text.take(400)}")
        null
    }
}

suspend inline fun <reified T> Response.tryDecodeBytesResponse(): ApiResponse<T>? {
    return when (status.toInt()) {
        200 -> {
            val buffer = arrayBuffer().await()
            val bytes = Int8Array(buffer).unsafeCast<ByteArray>()
            try {
                defaultCbor.decodeFromByteArray(
                    ApiResponseSerializer(serializer<T>()),
                    bytes
                )
            } catch (e: Exception) {
                console.log("failed to parse response:\n${e}\n${url}")
                null
            }
        }

        409 -> Problem("There was a conflict.")
        500 -> Problem("The server ran into a problem.")
        else -> Problem("Unknown error: $status")
    }
}

enum class EncodingType(val headerValue: String) {
    Cbor("application/cbor"),
    Json("application/json")
}