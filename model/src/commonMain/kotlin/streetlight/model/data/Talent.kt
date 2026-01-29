package streetlight.model.data

import kampfire.utils.randomUuidString
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
data class Talent(
    val talentId: TalentId,
    val name: String,
    val description: String?,
    val imageUrl: String?,
    val level: Int,
)

@JvmInline
@Serializable
value class TalentId(override val value: String): ProjectId {
    companion object { fun random() = TalentId(randomUuidString()) }
}