package koala

import kampfire.model.ImageSize
import kampfire.model.ImageVariant
import kampfire.model.Url
import kampfire.model.getSize
import kampfire.model.large
import kampfire.model.largest
import kampfire.model.medium
import kampfire.model.small
import kampfire.model.thumb
import kampfire.model.toUrl
import kotlinx.serialization.Serializable

@Serializable
data class Image(
    override val url: Url,
    val aspectRatio: Float? = null,
    val description: String? = null,
    val attribution: String? = null,
    val attributionUrl: Url? = null,
    val caption: String? = null,
    val variants: List<ImageVariant>? = null
): Asset {
    override val assetType get() = AssetType.Image
    override fun toString() = url.value

    val isRelative get() = url.isRelative
    val isAbsolute get() = url.isAbsolute
    val value get() = url.value
    val filename get() = url.filename

    fun getVariantOrNull(size: ImageSize) = variants.getSize(size)

    val thumb get() = variants.thumb
    val small get() = variants.small
    val medium get() = variants.medium
    val large get() = variants.large
    val largest get() = variants.largest
}

fun Url.toImage() = Image(this)
fun String.toImage() = Image(Url(this))

fun siteImageOf(
    path: String,
    aspectRatio: Float? = null,
    description: String? = null,
    attribution: String? = null,
    attributionUrl: String? = null,
    caption: String? = null,
    variants: List<ImageVariant>? = null
) = Image(
    url = siteImageUrlOf(path),
    aspectRatio = aspectRatio,
    attribution = attribution,
    attributionUrl = attributionUrl?.toUrl(),
    caption = caption,
    description = description,
    variants = variants,
)

fun siteImageUrlOf(path: String) = "$imgPath$path".toUrl()

fun Image?.getVariantOrPlaceholder(size: ImageSize) = this?.getVariantOrNull(size) ?: SiteImage.getPlaceholder(size)