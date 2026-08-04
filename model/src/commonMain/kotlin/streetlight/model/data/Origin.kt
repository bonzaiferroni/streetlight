package streetlight.model.data

import kampfire.api.TableId
import kampfire.model.Url
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable
data class Origin(
    val originId: OriginId, // eg. swallowhillmusic.org
    val schemas: List<OriginSchema>,
    val robotsTxt: String?,
    val updatedAt: Instant,
    val createdAt: Instant,
)

@JvmInline
@Serializable
value class OriginId(override val value: String): TableId<String> {
    override fun toString() = value
}

private val originRegex = Regex("^https?://(?:[^/?#@]*@)?(?:www\\.)?([^/?#:]+)", RegexOption.IGNORE_CASE)

fun Url.toOriginId(): OriginId? = takeIf { it.isAbsolute }?.let { url ->
    originRegex.find(url.value)
        ?.groupValues
        ?.get(1)
        ?.lowercase()
        ?.removeSuffix(".")
        ?.takeIf { it.isNotEmpty() }
        ?.let { OriginId(it) }
}


@Serializable
data class OriginSchema(
    val originSchemaId: OriginSchemaId,
    val originId: TableId<String>,
    val schemaType: SchemaType,
    val content: ContentSchema,
    val consecutiveFailCount: Int,
    val lastSuccessAt: Instant?,
    val updatedAt: Instant,
    val createdAt: Instant,
)

@JvmInline
@Serializable
value class OriginSchemaId(override val value: Uuid): RecordId {
    override fun toString() = value.toString()

    companion object { fun random() = OriginSchemaId(Uuid.random())}
}

enum class SchemaType {
    EventFeed,
    EventPage,
}

