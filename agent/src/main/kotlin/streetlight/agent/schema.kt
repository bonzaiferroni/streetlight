package streetlight.agent

import ai.koog.prompt.params.LLMParams
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import kotlinx.serialization.descriptors.*
import kotlin.reflect.KClass

inline fun <reified T: Any> KClass<T>.toBasicSchema(): LLMParams.Schema {
    return LLMParams.Schema.JSON.Basic(
        name = T::class.simpleName ?: error("must be a named type"),
        schema = toJsonSchema()
    )
}

@OptIn(InternalSerializationApi::class)
inline fun <reified T: Any> KClass<T>.toJsonSchema(): JsonObject {
    val serializer = serializer()
    val descriptor = serializer.descriptor

    require(descriptor.kind == StructureKind.CLASS) {
        "Only class-like types be supported"
    }

    val properties = buildMap<String, JsonElement> {
        for (i in 0 until descriptor.elementsCount) {
            val name = descriptor.getElementName(i)
            val elementDescriptor = descriptor.getElementDescriptor(i)
            put(name, elementDescriptor.toSchema())
        }
    }

    val required = buildList<JsonElement> {
        for (i in 0 until descriptor.elementsCount) {
            if (!descriptor.isElementOptional(i) &&
                !descriptor.getElementDescriptor(i).isNullable
            ) {
                add(JsonPrimitive(descriptor.getElementName(i)))
            }
        }
    }

    return JsonObject(
        buildMap {
            put("type", JsonPrimitive("object"))
            put("properties", JsonObject(properties))
            if (required.isNotEmpty()) {
                put("required", JsonArray(required))
            }
        }
    )
}

fun SerialDescriptor.toSchema(): JsonObject =
    buildJsonObject {
        when (kind) {
            PrimitiveKind.STRING -> put("type", JsonPrimitive("string"))
            PrimitiveKind.INT,
            PrimitiveKind.LONG,
            PrimitiveKind.SHORT -> put("type", JsonPrimitive("integer"))
            PrimitiveKind.FLOAT,
            PrimitiveKind.DOUBLE -> put("type", JsonPrimitive("number"))
            PrimitiveKind.BOOLEAN -> put("type", JsonPrimitive("boolean"))

            StructureKind.LIST -> {
                put("type", JsonPrimitive("array"))
                put("items", getElementDescriptor(0).toSchema())
            }

            StructureKind.CLASS -> {
                put("type", JsonPrimitive("object"))
                val nestedProps = buildMap<String, JsonElement> {
                    for (i in 0 until elementsCount) {
                        put(getElementName(i), getElementDescriptor(i).toSchema())
                    }
                }
                put("properties", JsonObject(nestedProps))
            }

            else -> put("type", JsonPrimitive("string"))
        }

        // Known temporal types → explicit formats + examples + pattern
        when (serialName) {
            "kotlinx.datetime.Instant" -> {
                put("type", JsonPrimitive("string"))
                put("format", JsonPrimitive("date-time"))
                put("examples", JsonArray(listOf(JsonPrimitive("2025-03-11T14:30:00Z"))))
            }
            "kotlinx.datetime.LocalDate" -> {
                put("type", JsonPrimitive("string"))
                put("format", JsonPrimitive("date"))
                put("pattern", JsonPrimitive("^\\d{4}-\\d{2}-\\d{2}$")) // YYYY-MM-DD
                put("examples", JsonArray(listOf(JsonPrimitive("2025-03-11"))))
            }
            "kotlinx.datetime.LocalTime" -> {
                put("type", JsonPrimitive("string"))
                put("format", JsonPrimitive("time"))
                put("pattern", JsonPrimitive("^\\d{2}:\\d{2}(:\\d{2})?$")) // HH:MM or HH:MM:SS
                put("examples", JsonArray(listOf(JsonPrimitive("19:30"), JsonPrimitive("19:30:00"))))
            }
        }
    }
