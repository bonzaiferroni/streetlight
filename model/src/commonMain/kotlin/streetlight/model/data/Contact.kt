package streetlight.model.data

import kabinet.db.TableId
import kabinet.utils.randomUuidString
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
data class Contact(
    val id: Int,
    val name: String,
    val phone: String,
    val email: String,
    val socialMedia: String,
)

@JvmInline
@Serializable
value class ContactId(override val value: String): ProjectId {
    companion object { fun random() = ContactId(randomUuidString()) }
}