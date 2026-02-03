package streetlight.model.data

import kampfire.model.GeoPoint
import kampfire.utils.randomUuidString
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
data class Community(
    val communityId: CommunityId,
    val name: String,
    val points: List<GeoPoint>,
    val communityType: CommunityType
) {
    companion object {
        val BFEastfax get() = Community(
            communityId = CommunityId.random(),
            name = "BF Eastfax",
            points = emptyList(),
            communityType = CommunityType.Street
        )
    }
}

@JvmInline @Serializable
value class CommunityId(override val value: String): ProjectId {
    companion object { fun random() = CommunityId(randomUuidString())}
}

enum class CommunityType {
    Street,
    Neighborhood,
    Campfire,
}