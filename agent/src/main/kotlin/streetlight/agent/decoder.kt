package streetlight.agent

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.json.*
import kotlinx.serialization.serializer

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
inline fun <reified T> decodeLenient(text: String): T {
    val serializer = serializer<T>()
    val root = LenientJson.parseToJsonElement(text)
    val sanitized = sanitize(root, serializer.descriptor)
    return LenientJson.decodeFromJsonElement(serializer, sanitized)
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
            if (sanitizedChild !is JsonNull) {
                put(name, sanitizedChild)
            } else {
                put(name, JsonNull)
            }
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
    val keyDesc = desc.getElementDescriptor(0)
    val valDesc = desc.getElementDescriptor(1)

    // JSON object keys are strings; if yer map key ain't a string, decoding will still be strict.
    // We'll sanitize values at least.
    val out = obj.mapValues { (_, v) -> sanitize(v, valDesc) }
    return JsonObject(out)
}

private fun sanitizePrimitive(element: JsonElement, desc: SerialDescriptor): JsonElement {
    val prim = element as? JsonPrimitive ?: return JsonNull

    // Handle kotlinx.datetime types by serialName
    when (desc.serialName) {
        "kotlinx.datetime.LocalDate" -> {
            val s = prim.contentOrNull?.trim() ?: return JsonNull
            return if (runCatching { LocalDate.parse(s) }.isSuccess) JsonPrimitive(s) else JsonNull
        }
        "kotlinx.datetime.LocalTime" -> {
            val s = prim.contentOrNull?.trim() ?: return JsonNull
            return if (runCatching { LocalTime.parse(s) }.isSuccess) JsonPrimitive(s) else JsonNull
        }
        "kotlinx.datetime.Instant" -> {
            // Accept ISO-8601, or epoch seconds/millis as number or numeric string.
            val s = prim.contentOrNull?.trim()
            if (!s.isNullOrBlank()) {
                if (runCatching { Instant.parse(s) }.isSuccess) return JsonPrimitive(s)
                val n = s.toLongOrNull()
                if (n != null) return JsonPrimitive(n) // keep numeric; serializer will decode
            }
            if (prim.longOrNull != null) return prim
            return JsonNull
        }
    }

    return when (desc.kind as PrimitiveKind) {
        PrimitiveKind.STRING -> JsonPrimitive(prim.contentOrNull ?: return JsonNull)

        PrimitiveKind.BOOLEAN -> {
            val b = prim.booleanOrNull ?: prim.contentOrNull
                ?.trim()
                ?.lowercase()
                ?.let {
                    when (it) {
                        "true", "t", "yes", "y", "1" -> true
                        "false", "f", "no", "n", "0" -> false
                        else -> null
                    }
                }
            if (b != null) JsonPrimitive(b) else JsonNull
        }

        PrimitiveKind.INT -> {
            val n = prim.intOrNull ?: prim.contentOrNull?.toIntOrNull()
            if (n != null) JsonPrimitive(n) else JsonNull
        }

        PrimitiveKind.LONG -> {
            val n = prim.longOrNull ?: prim.contentOrNull?.toLongOrNull()
            if (n != null) JsonPrimitive(n) else JsonNull
        }

        PrimitiveKind.FLOAT -> {
            val n = prim.floatOrNull ?: prim.contentOrNull?.toFloatOrNull()
            if (n != null) JsonPrimitive(n) else JsonNull
        }

        PrimitiveKind.DOUBLE -> {
            val n = prim.doubleOrNull ?: prim.contentOrNull?.toDoubleOrNull()
            if (n != null) JsonPrimitive(n) else JsonNull
        }

        PrimitiveKind.BYTE -> {
            val n = prim.intOrNull ?: prim.contentOrNull?.toIntOrNull()
            if (n != null && n in -128..127) JsonPrimitive(n) else JsonNull
        }

        PrimitiveKind.SHORT -> {
            val n = prim.intOrNull ?: prim.contentOrNull?.toIntOrNull()
            if (n != null && n in -32768..32767) JsonPrimitive(n) else JsonNull
        }

        PrimitiveKind.CHAR -> {
            val s = prim.contentOrNull
            if (s != null && s.length == 1) JsonPrimitive(s) else JsonNull
        }
    }
}