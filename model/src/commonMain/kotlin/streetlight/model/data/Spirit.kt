package streetlight.model.data

import kampfire.model.GeoPoint
import kampfire.utils.randomUuidString
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
data class Spirit(
    val spiritId: SpiritId,
    val geoPoint: GeoPoint,
    val label: String,
) {
}

@Serializable
@JvmInline
value class SpiritId(val value: String) {
    companion object {
        fun random() = SpiritId(randomUuidString())
    }
}

@Serializable
sealed interface SpiritDelta

@Serializable
data class SpiritPointDelta(val spiritId: SpiritId, val geoPoint: GeoPoint) : SpiritDelta