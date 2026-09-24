package kampfire.model

import kotlinx.serialization.Serializable

/** The URL of an image and of its thumbnail. */
@Serializable
data class ImageUrls(
    val url: String,
    val thumbUrl: String,
)