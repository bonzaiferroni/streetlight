package streetlight.model.data

import kampfire.api.Markdown
import kampfire.model.GeoPoint
import kampfire.model.Url
import kotlinx.serialization.Serializable

@Serializable
data class MediumEdit(
    val mediumId: MediumId? = null,
    val title: String? = null,
    val subtitle: String? = null,
    val text: Markdown? = null,
    val link: Url? = null,
    val geoPoint: GeoPoint? = null,
    val imageRef: Url? = null,
) {
    val isValid get() = true
    val invalidMessage: String? get() = null
}

fun Medium.toEdit() = MediumEdit(
    mediumId = mediumId,
    title = title,
    subtitle = subtitle,
    text = text,
    link = link,
    geoPoint = geoPoint,
    imageRef = imageRef,
)