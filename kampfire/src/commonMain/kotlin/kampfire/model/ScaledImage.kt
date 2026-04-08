package kampfire.model

import kotlinx.serialization.Serializable

@Serializable
data class ScaledImage(
    val size: ImageSize,
    val url: Url
)

typealias ScaledImageArray = List<ScaledImage>

val ScaledImageArray?.large get() = this?.firstOrNull { it.size == ImageSize.Large }?.url
val ScaledImageArray?.medium get() = this?.firstOrNull { it.size == ImageSize.Medium }?.url
val ScaledImageArray?.small get() = this?.firstOrNull { it.size == ImageSize.Small }?.url
val ScaledImageArray?.thumb get() = this?.firstOrNull { it.size == ImageSize.Thumb }?.url
