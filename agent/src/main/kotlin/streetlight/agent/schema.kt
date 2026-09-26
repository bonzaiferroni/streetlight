package streetlight.agent

import ai.koog.prompt.params.LLMParams
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import kotlinx.serialization.descriptors.*
import kotlin.reflect.KClass
import kotlin.reflect.KType

/** The standard JSON schema of this type, with every property required and a nullable one allowed to be `null`. */
fun KType.toStandardSchema(): LLMParams.Schema {
    return LLMParams.Schema.JSON.Standard(
        name = (classifier as? KClass<*>)?.simpleName ?: error("must be a named type"),
        schema = toJsonSchema()
    )
}

@OptIn(InternalSerializationApi::class)
fun KType.toJsonSchema(): JsonObject {
    val serializer = serializer(this)
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

    return JsonObject(
        buildMap {
            put("type", JsonPrimitive("object"))
            put("properties", JsonObject(properties))
            put("required", JsonArray(properties.keys.map { JsonPrimitive(it) }))
        }
    )
}

fun SerialDescriptor.toSchema(): JsonObject {
    val schema = buildJsonObject {
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
                put("required", JsonArray(nestedProps.keys.map { JsonPrimitive(it) }))
            }

            else -> put("type", JsonPrimitive("string"))
        }

        // Known temporal types → explicit formats + examples + pattern
        when (serialName) {
            "kotlin.datetime.Instant" -> {
                put("type", JsonPrimitive("string"))
                put("format", JsonPrimitive("date-time"))
                put("examples", JsonArray(listOf(JsonPrimitive("2025-03-11T14:30:00Z"))))
            }
            "kotlin.datetime.LocalDate" -> {
                put("type", JsonPrimitive("string"))
                put("format", JsonPrimitive("date"))
                put("pattern", JsonPrimitive("^\\d{4}-\\d{2}-\\d{2}$")) // YYYY-MM-DD
                put("examples", JsonArray(listOf(JsonPrimitive("2025-03-11"))))
            }
            "kotlin.datetime.LocalTime" -> {
                put("type", JsonPrimitive("string"))
                put("format", JsonPrimitive("time"))
                put("pattern", JsonPrimitive("^\\d{2}:\\d{2}(:\\d{2})?$")) // HH:MM or HH:MM:SS
                put("examples", JsonArray(listOf(JsonPrimitive("19:30"), JsonPrimitive("19:30:00"))))
            }
        }
    }
    if (!isNullable) return schema
    val type = schema["type"] ?: return schema
    return JsonObject(schema + ("type" to JsonArray(listOf(type, JsonPrimitive("null")))))
}
