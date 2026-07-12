package streetlight.model.data

import kampfire.api.Markdown
import kampfire.model.GeoPoint
import kampfire.model.Url
import koala.Image
import kotlinx.serialization.Serializable

@Serializable
data class MediaEdit(
    val mediaId: MediaId? = null,
    val title: String? = null,
    val subtitle: String? = null,
    val text: Markdown? = null,
    val link: Url? = null,
    val geoPoint: GeoPoint? = null,
    val image: Image? = null,
) {
    val isValid get() = true
    val invalidMessage: String? get() = null
}

fun Media.toEdit() = MediaEdit(
    mediaId = mediaId,
    title = title,
    subtitle = subtitle,
    text = text,
    link = link,
    geoPoint = geoPoint,
    image = image,
)