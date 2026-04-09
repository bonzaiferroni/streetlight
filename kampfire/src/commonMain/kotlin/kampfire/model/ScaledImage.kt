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