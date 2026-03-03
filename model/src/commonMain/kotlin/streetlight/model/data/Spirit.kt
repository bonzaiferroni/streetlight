package streetlight.model.data

import kampfire.model.GeoPoint
import kampfire.utils.randomInt
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import kotlin.jvm.JvmInline

@Serializable
data class Spirit(
    val spiritId: SpiritId,
    val position: GeoPoint,
    val name: String,
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
    data class Position(val id: SpiritId, val pos: GeoPoint): SpiritFrame
}
