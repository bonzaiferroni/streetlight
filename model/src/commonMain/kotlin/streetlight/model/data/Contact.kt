package streetlight.model.data

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.uuid.Uuid

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
value class ContactId(override val value: Uuid): RecordId {
    companion object { fun random() = ContactId(Uuid.random()) }
}