@file:OptIn(InternalSerializationApi::class)

package streetlight.agent

import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.json.*
import kotlinx.serialization.serializer
import kotlin.reflect.KClass
import kotlin.reflect.KType

val LenientJson = Json {
    ignoreUnknownKeys = true
    isLenient = true
    coerceInputValues = true
    explicitNulls = false
}

/**
 * Decode JSON into T, but if any field can't be parsed, it becomes null
 * (assuming the field is nullable/optional in your model).
 */
fun <T: Any> decodeLenient(text: String, type: KType): T {
    val serializer = serializer(type)
    val root = LenientJson.parseToJsonElement(text)
    val sanitized = sanitize(root, serializer.descriptor)
    @Suppress("UNCHECKED_CAST")
    return LenientJson.decodeFromJsonElement(serializer, sanitized) as T
}

fun sanitize(element: JsonElement, desc: SerialDescriptor): JsonElement {
    if (element is JsonNull) return JsonNull

    return when (desc.kind) {
        is PrimitiveKind -> sanitizePrimitive(element, desc)

        StructureKind.CLASS, StructureKind.OBJECT -> sanitizeClass(element, desc)

        StructureKind.LIST -> sanitizeList(element, desc)

        StructureKind.MAP -> sanitizeMap(element, desc)

        else -> element
    }
}

private fun sanitizeClass(element: JsonElement, desc: SerialDescriptor): JsonElement {
    val obj = element as? JsonObject ?: return JsonNull

    val out = buildMap<String, JsonElement> {
        for (i in 0 until desc.elementsCount) {
            val name = desc.getElementName(i)
            val childDesc = desc.getElementDescriptor(i)
            val childEl = obj[name] ?: continue

            val sanitizedChild = sanitize(childEl, childDesc)

            // If it failed to parse, turn it into null.
            // Nullable/optional fields will survive; non-nullable may still fail (as they should).
            put(name, sanitizedChild)
        }
    }

    // Keep unknown keys as-is; decoding ignores them anyway.
    // Merge unknowns back so you can re-emit if needed.
    val merged = obj.toMutableMap()
    merged.putAll(out)
    return JsonObject(merged)
}

private fun sanitizeList(element: JsonElement, desc: SerialDescriptor): JsonElement {
    val arr = element as? JsonArray ?: return JsonNull
    val itemDesc = desc.getElementDescriptor(0)
    return JsonArray(arr.map { sanitize(it, itemDesc) })
}

private fun sanitizeMap(element: JsonElement, desc: SerialDescriptor): JsonElement {
    val obj = element as? JsonObject ?: return JsonNull
    // val keyDesc = desc.getElementDescriptor(0)
    val valDesc = desc.getElementDescriptor(1)

    // JSON object keys are strings; if yer map key ain't a string, decoding will still be strict.
    // We'll sanitize values at least.
    val out = obj.mapValues { (_, v) -> sanitize(v, valDesc) }
    return JsonObject(out)
}

private fun sanitizePrimitive(element: JsonElement, desc: SerialDescriptor): JsonElement {
    val prim = element as? JsonPrimitive ?: return JsonNull

    val raw = prim.contentOrNull?.trim()

    // Treat blanks and common LLM "null-ish" strings as null
    if (raw.isNullOrEmpty()) return JsonNull
    if (raw.equals("null", ignoreCase = true)) return JsonNull
    if (raw.equals("undefined", ignoreCase = true)) return JsonNull

    // Nullable descriptors often have a "?" suffix in serialName
    val sn = desc.serialName.removeSuffix("?")

    when (sn) {
        "kotlinx.datetime.LocalDate" -> {
            return if (runCatching { LocalDate.parse(raw) }.isSuccess) JsonPrimitive(raw) else JsonNull
        }
        "kotlinx.datetime.LocalTime" -> {
            return if (runCatching { LocalTime.parse(raw) }.isSuccess) JsonPrimitive(raw) else JsonNull
        }
        "kotlin.time.Instant" -> {
            if (runCatching { Instant.parse(raw) }.isSuccess) return JsonPrimitive(raw)
            val n = raw.toLongOrNull()
            if (n != null) return JsonPrimitive(Instant.fromEpochSeconds(n).toString())
            return JsonNull
        }
    }

    return when (desc.kind as PrimitiveKind) {
        PrimitiveKind.STRING -> JsonPrimitive(raw)
        PrimitiveKind.BOOLEAN -> {
            val b = prim.booleanOrNull ?: raw.lowercase().let {
                when (it) {
                    "true", "t", "yes", "y", "1" -> true
                    "false", "f", "no", "n", "0" -> false
                    else -> null
                }
            }
            if (b != null) JsonPrimitive(b) else JsonNull
        }
        PrimitiveKind.INT -> prim.intOrNull?.let(::JsonPrimitive) ?: raw.toIntOrNull()?.let(::JsonPrimitive) ?: JsonNull
        PrimitiveKind.LONG -> prim.longOrNull?.let(::JsonPrimitive) ?: raw.toLongOrNull()?.let(::JsonPrimitive) ?: JsonNull
        PrimitiveKind.FLOAT -> prim.floatOrNull?.let(::JsonPrimitive) ?: raw.toFloatOrNull()?.let(::JsonPrimitive) ?: JsonNull
        PrimitiveKind.DOUBLE -> prim.doubleOrNull?.let(::JsonPrimitive) ?: raw.toDoubleOrNull()?.let(::JsonPrimitive) ?: JsonNull
        PrimitiveKind.BYTE -> (prim.intOrNull ?: raw.toIntOrNull())?.takeIf { it in -128..127 }?.let(::JsonPrimitive) ?: JsonNull
        PrimitiveKind.SHORT -> (prim.intOrNull ?: raw.toIntOrNull())?.takeIf { it in -32768..32767 }?.let(::JsonPrimitive) ?: JsonNull
        PrimitiveKind.CHAR -> raw.takeIf { it.length == 1 }?.let(::JsonPrimitive) ?: JsonNull
    }
}