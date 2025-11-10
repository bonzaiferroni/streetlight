package streetlight.model.data

import kabinet.utils.randomUuidString
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
data class Street(
    val streetId: StreetId,
    val name: String,
)

@JvmInline @Serializable
value class StreetId(override val value: String): ProjectId {
    companion object { fun random() = StreetId(randomUuidString())}
}