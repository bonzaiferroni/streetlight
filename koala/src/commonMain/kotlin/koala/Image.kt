package koala

import kampfire.model.Url
import kampfire.model.toUrl

data class Image(
    override val url: Url,
    val aspectRatio: Float? = null,
    val description: String? = null,
    val attribution: String? = null,
    val attributionUrl: Url? = null,
    val caption: String? = null,
): Asset {
    override val assetType get() = AssetType.Image
    override fun toString() = url.value
}

fun siteImageOf(
    path: String,
    aspectRatio: Float? = null,
    description: String? = null,
    attribution: String? = null,
    attributionUrl: String? = null,
    caption: String? = null
) = Image(
    url = "$imgPath$path".toUrl(),
    aspectRatio = aspectRatio,
    attribution = attribution,
    attributionUrl = attributionUrl?.toUrl(),
    caption = caption,
    description = description
)