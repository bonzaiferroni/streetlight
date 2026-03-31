package streetlight.model.data

import kampfire.model.GeoPoint
import kampfire.utils.randomUuidString
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
data class Galaxy(
    val galaxyId: GalaxyId,
    val path: String,
    val name: String,
    val description: String?,
    val center: GeoPoint,
    val imageUrl: String?,
    val thumbUrl: String?,
    val updatedAt: Instant,
    val createdAt: Instant,
)

@JvmInline @Serializable
value class GalaxyId(override val value: String): ProjectId {
    companion object { fun random() = GalaxyId(randomUuidString())}
    override fun toString() = value
}

@Serializable
data class GalaxyEdit(
    val name: String? = null,
    val path: String? = null,
    val description: String? = null,
    val center: GeoPoint? = null,
    val imageUrl: String? = null,
    val thumbUrl: String? = null,
) {
    val isValid get() = !name.isNullOrBlank() && center != null

    companion object {
        val NameCharacters = setOf(' ', '.', ',', '\'', '!', '?', '-')
        val PathCharacters = setOf('_')

        fun isValidName(name: String) = name.all { it.isDigit() || it.isLetter() || NameCharacters.contains(it) }
                && name.length <= MAX_NAME_LENGTH
        fun isValidPath(path: String) = path.all { it.isDigit() || it.isLetter() || PathCharacters.contains(it) }

        fun pathOf(name: String): String =
            name
                .trim()
                .lowercase()
                .replace("\\s+".toRegex(), "_")
                .replace("[^a-z0-9_\\-]".toRegex(), "")

        const val MAX_NAME_LENGTH = 24
    }
}