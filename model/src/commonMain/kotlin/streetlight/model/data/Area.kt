package streetlight.model.data

import kabinet.utils.randomUuidString
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
data class Area(
    val areaId: AreaId,
    val name: String,
)

@JvmInline @Serializable
value class AreaId(override val value: String): ProjectId {
    companion object { fun random() = AreaId(randomUuidString())}
}