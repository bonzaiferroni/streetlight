package streetlight.model.data

import kampfire.api.Markdown
import kampfire.model.GeoPoint
import kampfire.model.ScaledImageArray
import koala.Svg
import koala.html.AppRoute
import kotlinx.serialization.Serializable

@Serializable
sealed interface StreetPost {
    val label: String
    val geoPoint: GeoPoint?
    val images: ScaledImageArray? get() = null
    val sublabel: String? get() = null
    val body: Markdown? get() = null
    val links: List<ExtraLink>? get() = null
}