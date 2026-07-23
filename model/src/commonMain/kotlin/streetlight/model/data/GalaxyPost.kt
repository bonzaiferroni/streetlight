package streetlight.model.data

import kampfire.model.Labeled
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.uuid.Uuid

@Serializable
sealed interface GalaxyPost: Entity {
    val base: Post
}

@Serializable
@JvmInline
value class PostId(override val value: Uuid): RecordId {
    override fun toString() = value.toString()

    companion object {
        fun random() = PostId(Uuid.random())
    }
}

enum class PostOrder(label: String? = null): Labeled {
    NewFirst("Newest first"),
    OldFirst("Oldest first");
    // Visibility;

    override val label = label ?: name
}

@Serializable
data class LocationPost(
    val location: Location,
    override val base: Post,
): GalaxyPost, Entity by location

@Serializable
data class EventPost(
    val event: EventLocation,
    override val base: Post,
): GalaxyPost, Entity by event