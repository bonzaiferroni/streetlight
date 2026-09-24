package koala

import kampfire.api.TableId
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
import kotlin.jvm.JvmInline
import kotlin.uuid.Uuid

/** An image with its stored size variants and the details shown with it: description, attribution and caption. */
@Serializable
data class Image(
    override val url: Url,
    val imageId: ImageId? = null,
    val variants: List<ImageVariant>? = null,
    val name: String? = null,
    val aspect: Float? = null,
    val description: String? = null,
    val attribution: String? = null,
    val attributionUrl: Url? = null,
    val caption: String? = null,
): Asset {
    override val assetType get() = AssetType.Image
    override fun toString() = url.value

    val isRelative get() = url.isRelative
    val isAbsolute get() = url.isAbsolute
    val value get() = url.value
    val filename get() = url.filename

    /** The URL of the variant at exactly [size], or `null`. */
    fun getSizeOrNull(size: ImageSize) = variants.getSize(size)

    val thumb get() = variants.thumb
    val small get() = variants.small
    val medium get() = variants.medium
    val large get() = variants.large
    val largest get() = variants.largest
}

/** The id of a stored image. */
@JvmInline
@Serializable
value class ImageId(override val value: Uuid): TableId<Uuid> {
    override fun toString() = value.toString()

    companion object { fun random() = ImageId(Uuid.random())}
}

/** An [Image] of this URL, with no variants. */
fun Url.toImage() = Image(this)
/** An [Image] of this URL, with no variants. */
fun String.toImage() = Image(Url(this))

/** An [Image] of the site's own file at [path], with its details. */
fun siteImageOf(
    path: String,
    aspectRatio: Float? = null,
    description: String? = null,
    attribution: String? = null,
    attributionUrl: String? = null,
    caption: String? = null,
    variants: List<ImageVariant>? = listOf(ImageVariant(ImageSize.Large, siteImageUrlOf(path))),
    name: String = path.filenameWithoutExtension(),
) = Image(
    url = siteImageUrlOf(path),
    variants = variants,
    name = name,
    aspect = aspectRatio,
    attribution = attribution,
    attributionUrl = attributionUrl?.toUrl(),
    caption = caption,
    description = description,
)

/** The URL of the site's own image at [path]. */
fun siteImageUrlOf(path: String) = "$imgPath$path".toUrl()

/** The URL of the variant at [size], or the placeholder at that size. */
fun Image?.getVariantOrPlaceholder(size: ImageSize) = this?.getSizeOrNull(size) ?: SiteImage.getPlaceholder(size)

/** This image, with any detail it lacks taken from [image]. */
fun Image.merge(image: Image) = Image(
    url = this.url,
    aspect = this.aspect ?: image.aspect,
    description = this.description ?: image.description,
    attribution = this.attribution ?: image.attribution,
    attributionUrl = this.attributionUrl ?: image.attributionUrl,
    caption = this.caption ?: image.caption,
    variants = this.variants ?: image.variants
)

/** The last path segment without its extension. */
fun String.filenameWithoutExtension(): String = substringAfterLast('/').substringBeforeLast('.')