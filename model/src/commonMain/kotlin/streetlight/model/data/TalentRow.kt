package streetlight.model.data

import kampfire.model.UserId
import kampfire.utils.randomUuidString
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

@Serializable
data class NewTalent(
    val name: String = "",
    val description: String? = null,
    val imageUrl: String? = null,
    val talentType: TalentType = TalentType.Music,
)