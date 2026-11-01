package streetlight.model.data

import kampfire.api.Markdown
import kampfire.model.GeoPoint
import kampfire.model.Url
import koala.Image
import kotlinx.serialization.Serializable

@Serializable
data class MediaEdit(
    val mediaId: MediaId? = null,
    val mediaType: MediaType = MediaType.Text,
    val title: String? = null,
    val subtitle: String? = null,
    val text: Markdown? = null,
    val url: Url? = null,
    val meta: MediaMeta? = null,
    val geoPoint: GeoPoint? = null,
    val image: Image? = null,
    val design: PageDesign? = null,
    /** The skills to link the media to; `null` leaves its links as they are. */
    val skillIds: List<SkillId>? = null,
) {
    val isValid get() = true
    val invalidMessage: String? get() = null
}

fun Media.toEdit() = MediaEdit(
    mediaId = mediaId,
    mediaType = mediaType,
    title = title,
    subtitle = subtitle,
    text = text,
    url = url,
    meta = meta,
    geoPoint = geoPoint,
    image = image,
)