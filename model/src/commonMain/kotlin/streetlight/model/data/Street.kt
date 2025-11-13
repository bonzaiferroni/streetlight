package streetlight.model.data

import kabinet.model.GeoPoint
import kabinet.utils.randomUuidString
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
data class Street(
    val streetId: StreetId,
    val name: String,
    val points: List<GeoPoint>
)

@JvmInline @Serializable
value class StreetId(override val value: String): ProjectId {
    companion object { fun random() = StreetId(randomUuidString())}
}