package streetlight.model.data

import kampfire.api.Markdown
import kampfire.api.Slug
import kampfire.api.Username
import kotlin.time.Instant
import kotlinx.serialization.Serializable

@Serializable
data class LocationPost(
    val location: Location,
    override val base: Post,
): GalaxyPost {
    override val images get() = location.images
    override val geoPoint get() = location.geoPoint
    override val body get() = location.description
    override val label get() = location.name ?: "[location removed]"
    override val sublabel get() = location.sublabel
    override val links get() = location.extraLinks
}

// @Serializable
// data class LocationPostEdit(
//     val postId: PostId?,
//     val galaxyId: GalaxyId,
//     val locationId: LocationId,
//     val text: Markdown? = null,
// ) {
//     val isValid get () = true // !title.isNullOrBlank()
// }
