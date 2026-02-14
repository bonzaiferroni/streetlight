package streetlight.model.data

import kampfire.utils.randomUuidString
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
data class Story(
    val storyId: StoryId,
    val headline: String,
    val url: String,
    val imageUrl: String,
    val iconUrl: String,
    val visibility: Int,
    val createdAt: Instant,
    val updatedAt: Instant,
)

@JvmInline
@Serializable
value class StoryId(override val value: String): ProjectId {
    companion object { fun random() = StoryId(randomUuidString())}
}

