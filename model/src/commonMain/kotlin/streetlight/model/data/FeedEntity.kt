package streetlight.model.data

import kampfire.api.Markdown
import kampfire.model.GeoPoint
import koala.Image
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
sealed interface FeedEntity {
    val recordId: Uuid? get() = null
    val label: String
    val geoPoint: GeoPoint?
    val image: Image? get() = null
    val sublabel: String? get() = null
    val body: Markdown? get() = null
    val links: List<ExtraLink>? get() = null
}