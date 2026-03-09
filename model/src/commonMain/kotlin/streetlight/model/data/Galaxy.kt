package streetlight.model.data

import kampfire.model.GeoPoint
import kampfire.utils.randomUuidString
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
data class Galaxy(
    val galaxyId: GalaxyId,
    val name: String,
    val center: GeoPoint,
) {
    companion object {
        val Eastfax get() = Galaxy(
            galaxyId = GalaxyId.random(),
            name = "BF Eastfax",
            center = GeoPoint.Denver
        )
    }
}

@JvmInline @Serializable
value class GalaxyId(override val value: String): ProjectId {
    companion object { fun random() = GalaxyId(randomUuidString())}
}