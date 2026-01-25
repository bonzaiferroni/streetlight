package kampfire.model

import kotlinx.serialization.Serializable

@Serializable
data class ImageUrls(
    val url: String,
    val thumbUrl: String,
)