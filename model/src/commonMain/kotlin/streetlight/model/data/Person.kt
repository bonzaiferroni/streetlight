package streetlight.model.data

import kampfire.model.UserId
import kampfire.utils.randomUuidString
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
data class Person(
    val personId: PersonId,
    val userId: UserId,
    val venmo: String,
    val stageName: String,
)

@JvmInline @Serializable
value class PersonId(override val value: String): ProjectId {
    companion object { fun random() = PersonId(randomUuidString()) }
}
