package kampfire.model

import kotlinx.serialization.Serializable

/** The URL of an image at one [ImageSize]. */
@Serializable
data class ImageVariant(
    val size: ImageSize,
    val url: Url
)

typealias ImageVariants = List<ImageVariant>

/** The large variant, or the nearest smaller one. */
val ImageVariants?.large get() = target(ImageSize.Large)
val ImageVariants?.medium get() = target(ImageSize.Medium)
val ImageVariants?.small get() = target(ImageSize.Small)
val ImageVariants?.thumb get() = target(ImageSize.Thumb)

/** The URL of the variant at exactly [size], or `null`. */
fun ImageVariants?.getSize(size: ImageSize) = when (size) {
    ImageSize.Thumb -> this?.firstOrNull { it.size == ImageSize.Thumb }?.url
    ImageSize.Small -> this?.firstOrNull { it.size == ImageSize.Small }?.url
    ImageSize.Medium -> this?.firstOrNull { it.size == ImageSize.Medium }?.url
    ImageSize.Large -> this?.firstOrNull { it.size == ImageSize.Large }?.url
}

/** The URL of the variant at [size], or the nearest larger one. */
fun ImageVariants.getSizeOrLarger(size: ImageSize): Url? {
    for (i in size.ordinal until ImageSize.entries.size) {
        val candidate = ImageSize.entries[i]
        val url = firstOrNull { it.size == candidate }?.url
        if (url != null) return url
    }
    return null
}

/** The URL of the variant at [size], or the nearest smaller one. */
fun ImageVariants?.target(size: ImageSize) = when (size) {
    ImageSize.Thumb -> getSize(ImageSize.Thumb)
    ImageSize.Small -> getSize(ImageSize.Small) ?: getSize(ImageSize.Thumb)
    ImageSize.Medium -> getSize(ImageSize.Medium) ?: getSize(ImageSize.Small) ?: getSize(ImageSize.Thumb)
    ImageSize.Large -> getSize(ImageSize.Large) ?: getSize(ImageSize.Medium) ?: getSize(ImageSize.Small) ?: getSize(ImageSize.Thumb)
}

/** The URL of the largest variant. */
val ImageVariants?.largest get() = large ?: medium ?: small ?: thumb

/** The variants as an HTML `srcset`, leaving out the square thumbnail. */
fun ImageVariants.toHtmlSrcSet() = buildString {
    val entries = this@toHtmlSrcSet.filter { it.size != ImageSize.Thumb }
    entries.forEachIndexed { i, img ->
        append("${img.url} ${img.size.widthPx}w")
        if (i < entries.lastIndex) append(", ")
    }
}

/** The variants as an HTML `sizes` value, leaving out the square thumbnail. */
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