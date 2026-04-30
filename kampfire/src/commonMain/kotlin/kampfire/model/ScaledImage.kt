package kampfire.model

import kotlinx.serialization.Serializable

@Serializable
data class ScaledImage(
    val size: ImageSize,
    val url: Url
)

typealias ScaledImageArray = List<ScaledImage>

val ScaledImageArray?.large get() = target(ImageSize.Large)
val ScaledImageArray?.medium get() = target(ImageSize.Medium)
val ScaledImageArray?.small get() = target(ImageSize.Small)
val ScaledImageArray?.thumb get() = target(ImageSize.Thumb)

fun ScaledImageArray?.getSize(size: ImageSize) = when (size) {
    ImageSize.Thumb -> this?.firstOrNull { it.size == ImageSize.Thumb }?.url
    ImageSize.Small -> this?.firstOrNull { it.size == ImageSize.Small }?.url
    ImageSize.Medium -> this?.firstOrNull { it.size == ImageSize.Medium }?.url
    ImageSize.Large -> this?.firstOrNull { it.size == ImageSize.Large }?.url
}
fun ScaledImageArray?.target(size: ImageSize) = when (size) {
    ImageSize.Thumb -> getSize(ImageSize.Thumb)
    ImageSize.Small -> getSize(ImageSize.Small) ?: getSize(ImageSize.Thumb)
    ImageSize.Medium -> getSize(ImageSize.Medium) ?: getSize(ImageSize.Small) ?: getSize(ImageSize.Thumb)
    ImageSize.Large -> getSize(ImageSize.Large) ?: getSize(ImageSize.Medium) ?: getSize(ImageSize.Small) ?: getSize(ImageSize.Thumb)
}

val ScaledImageArray?.largest get() = large ?: medium ?: small ?: thumb

fun ScaledImageArray.toHtmlSrcSet() = buildString {
    val entries = this@toHtmlSrcSet.filter { it.size != ImageSize.Thumb }
    entries.forEachIndexed { i, img ->
        append("${img.url} ${img.size.widthPx}w")
        if (i < entries.lastIndex) append(", ")
    }
}

fun ScaledImageArray.toHtmlSizes() = buildString {
    val entries = this@toHtmlSizes.filter { it.size != ImageSize.Thumb }
        .sortedByDescending { it.size.minWidthPx }
    entries.forEachIndexed { i, img ->
        if (i < entries.lastIndex) {
            append("(min-width: ${img.size.minWidthPx}px) ${img.size.widthPx}px, ")
        } else {
            append("${img.size.widthPx}px")
        }
    }
}