package kampfire.model

import kotlinx.serialization.Serializable

@Serializable
data class ImageVariant(
    val size: ImageSize,
    val url: Url
)

typealias ImageVariants = List<ImageVariant>

val ImageVariants?.large get() = target(ImageSize.Large)
val ImageVariants?.medium get() = target(ImageSize.Medium)
val ImageVariants?.small get() = target(ImageSize.Small)
val ImageVariants?.thumb get() = target(ImageSize.Thumb)

fun ImageVariants?.getSize(size: ImageSize) = when (size) {
    ImageSize.Thumb -> this?.firstOrNull { it.size == ImageSize.Thumb }?.url
    ImageSize.Small -> this?.firstOrNull { it.size == ImageSize.Small }?.url
    ImageSize.Medium -> this?.firstOrNull { it.size == ImageSize.Medium }?.url
    ImageSize.Large -> this?.firstOrNull { it.size == ImageSize.Large }?.url
}
fun ImageVariants?.target(size: ImageSize) = when (size) {
    ImageSize.Thumb -> getSize(ImageSize.Thumb)
    ImageSize.Small -> getSize(ImageSize.Small) ?: getSize(ImageSize.Thumb)
    ImageSize.Medium -> getSize(ImageSize.Medium) ?: getSize(ImageSize.Small) ?: getSize(ImageSize.Thumb)
    ImageSize.Large -> getSize(ImageSize.Large) ?: getSize(ImageSize.Medium) ?: getSize(ImageSize.Small) ?: getSize(ImageSize.Thumb)
}

val ImageVariants?.largest get() = large ?: medium ?: small ?: thumb

fun ImageVariants.toHtmlSrcSet() = buildString {
    val entries = this@toHtmlSrcSet.filter { it.size != ImageSize.Thumb }
    entries.forEachIndexed { i, img ->
        append("${img.url} ${img.size.widthPx}w")
        if (i < entries.lastIndex) append(", ")
    }
}

fun ImageVariants.toHtmlSizes() = buildString {
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