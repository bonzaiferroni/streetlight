package streetlight.model.data

import kampfire.api.Markdown
import kampfire.model.GeoPoint
import kampfire.model.ScaledImageArray
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.uuid.Uuid

@Serializable
sealed interface GalaxyPost {
    val base: Post
    val images: ScaledImageArray?
    val geoPoint: GeoPoint?
    val label: String
    val sublabel: String?
    val body: Markdown?
    val links: List<ExtraLink>?
}

@Serializable
@JvmInline
value class PostId(override val value: Uuid): RecordId {
    override fun toString() = value.toString()

    companion object {
        fun random() = PostId(Uuid.random())
    }
}

enum class PostOrder(label: String? = null) {
    NewFirst("Newest first"),
    OldFirst("Oldest first");
    // Visibility;

    val label = label ?: name
}