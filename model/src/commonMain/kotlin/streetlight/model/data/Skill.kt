package streetlight.model.data

import koala.model.ExtraLink
import kampfire.api.Markdown
import kampfire.api.Username
import kampfire.api.toMarkdown
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
    override val username: Username?,
    val name: String,
    override val description: Markdown?,
    override val image: Image?,
    val experience: Int,
    val tags: List<SkillTag>?,
    val interview: List<InterviewQuestion>,
    override val links: List<ExtraLink>?,
    val skillLevel: SkillLevel,
    val yearStarted: Int,
    val updatedAt: Instant,
    override val createdAt: Instant,
): Entity {
    override val label get() = name
    override val body get() = description ?: "${skillLevel.label} since $yearStarted".toMarkdown()
    override val geoPoint get() = null
    override val markerId get() = skillId.value.toString()
}

@JvmInline
@Serializable
value class SkillId(override val value: Uuid): RecordId {
    companion object { fun random() = SkillId(Uuid.random()) }

    override fun toString() = value.toString()
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
    val links: List<ExtraLink>? = null,
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
    links = links,
    skillLevel = skillLevel,
    yearStarted = yearStarted,
)