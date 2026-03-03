package streetlight.model.data

import kampfire.model.GeoPoint
import kampfire.utils.randomUuidString
import kotlin.jvm.JvmInline

data class Spirit(
    val spiritId: SpiritId,
    val geoPoint: GeoPoint,
    val label: String,
) {
}

@JvmInline
value class SpiritId(val value: String) {
    companion object {
        fun random() = SpiritId(randomUuidString())
    }
}