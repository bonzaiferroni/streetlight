package streetlight.model.data

import kampfire.api.Markdown
import kampfire.model.GeoPoint
import kampfire.model.ScaledImageArray
import kotlinx.serialization.Serializable

@Serializable
sealed interface StreetPost {
    val images: ScaledImageArray?
    val geoPoint: GeoPoint?
    val label: String
    val sublabel: String?
    val body: Markdown?
    val links: List<ExtraLink>?
}