@file:OptIn(ExperimentalSerializationApi::class)

package streetlight.web.io

import js.typedarrays.Int8Array
import kampfire.model.Outcome
import kampfire.model.OutcomeSerializer
import kampfire.model.Ok
import kampfire.model.Problem
import koala.utils.jsonConfig
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.serializer
import web.http.Response
import streetlight.model.data.RecordId
import streetlight.model.data.toRecordId
import web.http.arrayBuffer
import web.http.text

suspend inline fun <reified Returned> Response.tryDecode(encoding: EncodingType?): Outcome<Returned> {
    return when (encoding) {
        EncodingType.Cbor -> decodeBytes()
        EncodingType.Json, null -> decodeText()
    }
}

suspend inline fun <reified Returned> Response.decodeText(): Outcome<Returned> {
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

    val text = text()

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

suspend inline fun <reified T> Response.decodeBytes(): Outcome<T> {
    return when (status.toInt()) {
        200 -> {
            val buffer = arrayBuffer()
            val bytes = Int8Array(buffer).unsafeCast<ByteArray>()
            try {
                defaultCbor.decodeFromByteArray(
                    OutcomeSerializer(serializer<T>()),
                    bytes
                )
            } catch (e: Exception) {
                console.log("failed to parse response:\n${e}\n${url}")
                Problem("Unable to parse response.")
            }
        }

        else -> Problem("Unhandled error: $status")
    }
}

enum class EncodingType(val headerValue: String) {
    Cbor("application/cbor"),
    Json("application/json"),
    // OctetStream("application/octet-stream")
}