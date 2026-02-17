package streetlight.model.data

import kotlinx.serialization.Serializable

@Serializable
data class NewCommunity(
    val name: String,
    val communityType: CommunityType
)