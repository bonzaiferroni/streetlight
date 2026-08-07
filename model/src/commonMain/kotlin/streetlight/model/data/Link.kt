package streetlight.model.data

import kampfire.model.Url
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable
data class Link(
    val linkId: LinkId,
    val originId: OriginId,
    val url: Url,
    val schemaType: SchemaType?,
    val fetchedAt: Instant,
    val createdAt: Instant,
)

@JvmInline
@Serializable
value class LinkId(override val value: Uuid): RecordId {
    override fun toString() = value.toString()
}

@Serializable
data class LinkAlias(
    val linkAliasId: LinkAliasId,
    val linkId: LinkId,
    val url: Url,
    val createdAt: Instant,
)

@JvmInline
@Serializable
value class LinkAliasId(override val value: Uuid): RecordId {
    override fun toString() = value.toString()
}