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
data class Skill(
    val skillId: SkillId,
    val name: String,
    val description: Markdown?,
    val image: Image?,
    val experience: Int,
    val tags: List<SkillTag>?,
    val interview: List<InterviewQuestion>,
    val skillLevel: SkillLevel,
    val yearStarted: Int,
    val updatedAt: Instant,
    val createdAt: Instant,
)

@JvmInline
@Serializable
value class SkillId(override val value: Uuid): RecordId {
    companion object { fun random() = SkillId(Uuid.random()) }
}

/** The level of a [Skill]. */
enum class SkillLevel: Labeled {
    Beginner,
    Intermediate,
    Professional;

    override val label get() = name
}

/** The fields of a skill a form sends. */
@Serializable
data class SkillEdit(
    val skillId: SkillId? = null,
    val name: String = "",
    val description: Markdown? = null,
    val image: Image? = null,
    val tags: List<SkillTag> = emptyList(),
    val interview: List<InterviewQuestion> = emptyList(),
    val skillLevel: SkillLevel = SkillLevel.Beginner,
    val yearStarted: Int = 2025,
)

/** An edit of this skill, starting from its current values. */
fun Skill.toEdit() = SkillEdit(
    skillId = skillId,
    name = name,
    description = description,
    image = image,
    tags = tags.orEmpty(),
    interview = interview,
    skillLevel = skillLevel,
    yearStarted = yearStarted,
)