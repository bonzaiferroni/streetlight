package streetlight.model.data

import kotlin.time.Instant
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.uuid.Uuid

/** A skill a user offers, with their experience. */
@Serializable
data class Talent(
    val talentId: TalentId,
    val name: String,
    val description: String?,
    val imageUrl: String?,
    val experience: Int,
    val talentType: TalentType,
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

/** The kinds of [Talent]. */
enum class TalentType {
    Music,
    Performance,
    Promotion,
    Food,
    Technical,
    Host,
    Other,
}

/** The level of a [Talent]. */
enum class TalentLevel {
    Beginner,
    Intermediate,
    Professional,
}

/** The fields of a talent a form sends. */
@Serializable
data class TalentEdit(
    val talentId: TalentId? = null,
    val name: String = "",
    val description: String? = null,
    val imageUrl: String? = null,
    val talentType: TalentType = TalentType.Music,
    val talentLevel: TalentLevel = TalentLevel.Beginner,
    val yearStarted: Int = 2025,
)

/** An edit of this talent, starting from its current values. */
fun Talent.toEdit() = TalentEdit(
    talentId = talentId,
    name = name,
    description = description,
    imageUrl = imageUrl,
    talentType = talentType,
    talentLevel = talentLevel,
    yearStarted = yearStarted,
)