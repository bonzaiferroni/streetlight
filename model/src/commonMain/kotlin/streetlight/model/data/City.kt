package streetlight.model.data

import kampfire.utils.randomUuidString
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.time.Instant

@Serializable
data class City(
    val cityId: CityId,
    val name: String,
    val galaxyCount: Int,
)

@JvmInline @Serializable
value class CityId(override val value: String): ProjectId {
    companion object { fun random() = CityId(randomUuidString())}
    override fun toString() = value
}