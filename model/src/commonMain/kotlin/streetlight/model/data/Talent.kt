package streetlight.model.data

import kampfire.utils.randomUuidString
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

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
value class TalentId(override val value: String): ProjectId {
    companion object { fun random() = TalentId(randomUuidString()) }
}

enum class TalentType {
    Music,
    Performance,
    Promotion,
    Food,
    Technical,
    Host,
    Other,
}

enum class TalentLevel {
    Beginner,
    Intermediate,
    Professional,
}

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

fun Talent.toEdit() = TalentEdit(
    talentId = talentId,
    name = name,
    description = description,
    imageUrl = imageUrl,
    talentType = talentType,
    talentLevel = talentLevel,
    yearStarted = yearStarted,
)