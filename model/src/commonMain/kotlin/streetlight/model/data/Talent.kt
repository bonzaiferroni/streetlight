package streetlight.model.data

import kampfire.api.Markdown
import kampfire.model.Labeled
import koala.Image
import kotlin.time.Instant
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.uuid.Uuid

/** A skill a user offers, with their experience. */
@Serializable
data class Talent(
    val talentId: TalentId,
    val name: String,
    val description: Markdown?,
    val image: Image?,
    val experience: Int,
    val tags: List<TalentTag>?,
    val interview: List<InterviewQuestion>,
    val talentLevel: TalentLevel,
    val yearStarted: Int,
    val updatedAt: Instant,
    val createdAt: Instant,
)

@JvmInline
@Serializable
value class TalentId(override val value: Uuid): RecordId {
    companion object { fun random() = TalentId(Uuid.random()) }
}

/** The level of a [Talent]. */
enum class TalentLevel: Labeled {
    Beginner,
    Intermediate,
    Professional;

    override val label get() = name
}

/** The fields of a talent a form sends. */
@Serializable
data class TalentEdit(
    val talentId: TalentId? = null,
    val name: String = "",
    val description: Markdown? = null,
    val image: Image? = null,
    val tags: List<TalentTag> = emptyList(),
    val interview: List<InterviewQuestion> = emptyList(),
    val talentLevel: TalentLevel = TalentLevel.Beginner,
    val yearStarted: Int = 2025,
)

/** An edit of this talent, starting from its current values. */
fun Talent.toEdit() = TalentEdit(
    talentId = talentId,
    name = name,
    description = description,
    image = image,
    tags = tags.orEmpty(),
    interview = interview,
    talentLevel = talentLevel,
    yearStarted = yearStarted,
)