package streetlight.model.data

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable
data class Parser(
    val parserId: ParserId,
    val originId: OriginId,
    val schemaType: SchemaType,
    val fetchMode: FetchMode,
    val schema: SelectorSchema,
    val consecutiveFailCount: Int,
    val lastSuccessAt: Instant?,
    val updatedAt: Instant,
    val createdAt: Instant,
)

@JvmInline
@Serializable
value class ParserId(override val value: Uuid): RecordId {
    override fun toString() = value.toString()

    companion object { fun random() = ParserId(Uuid.random())}
}

enum class SchemaType {
    EventFeed,
    EventPage,
}