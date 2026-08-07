package streetlight.model.data

import kampfire.api.Markdown
import kampfire.model.GeoPoint
import koala.Image
import kotlinx.serialization.Serializable

@Serializable
sealed interface FeedEntity {
    val label: String
    val geoPoint: GeoPoint?
    val image: Image? get() = null
    val sublabel: String? get() = null
    val body: Markdown? get() = null
    val links: List<ExtraLink>? get() = null
}