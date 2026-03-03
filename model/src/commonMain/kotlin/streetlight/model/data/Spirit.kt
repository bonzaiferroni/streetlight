package streetlight.model.data

import kampfire.model.GeoPoint
import kampfire.utils.randomInt
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
value class SpiritId(val value: Int) {
    companion object {
        fun random() = SpiritId(randomInt())
    }
}

@Serializable
sealed interface SpiritFrame {
    @Serializable
    data class Initial(val spirit: Spirit): SpiritFrame

    @Serializable
    data class PointDelta(val spiritId: SpiritId, val point: GeoPoint): SpiritFrame
}