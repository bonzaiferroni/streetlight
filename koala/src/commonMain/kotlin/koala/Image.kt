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

    fun getSizeOrNull(size: ImageSize) = variants.getSize(size)

    val thumb get() = variants.thumb
    val small get() = variants.small
    val medium get() = variants.medium
    val large get() = variants.large
    val largest get() = variants.largest
}

@JvmInline
@Serializable
value class ImageId(override val value: Uuid): TableId<Uuid> {
    override fun toString() = value.toString()

    companion object { fun random() = ImageId(Uuid.random())}
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
    variants: List<ImageVariant>? = null,
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

fun siteImageUrlOf(path: String) = "$imgPath$path".toUrl()

fun Image?.getVariantOrPlaceholder(size: ImageSize) = this?.getSizeOrNull(size) ?: SiteImage.getPlaceholder(size)

fun Image.merge(image: Image) = Image(
    url = this.url,
    aspect = this.aspect ?: image.aspect,
    description = this.description ?: image.description,
    attribution = this.attribution ?: image.attribution,
    attributionUrl = this.attributionUrl ?: image.attributionUrl,
    caption = this.caption ?: image.caption,
    variants = this.variants ?: image.variants
)

fun String.filenameWithoutExtension(): String = substringAfterLast('/').substringBeforeLast('.')