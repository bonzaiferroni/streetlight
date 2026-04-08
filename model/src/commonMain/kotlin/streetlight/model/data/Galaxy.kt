package streetlight.model.data

import kampfire.model.GeoPoint
import kampfire.model.ScaledImageArray
import kampfire.model.Url
import kampfire.utils.randomUuidString
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
data class Galaxy(
    val galaxyId: GalaxyId,
    val slug: Slug,
    val name: String,
    val description: String?,
    val center: GeoPoint,
    val zoom: Float,
    val postPermission: PostPermission,
    val reviewMode: ReviewMode,
    val postGuide: String?,
    val imageRef: Url?,
    val images: ScaledImageArray?,
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
    val galaxyId: GalaxyId? = null,
    val name: String? = null,
    val slug: Slug? = null,
    val description: String? = null,
    val center: GeoPoint? = null,
    val zoom: Float? = null,
    val postPermission: PostPermission = PostPermission.Accounts,
    val reviewMode: ReviewMode = ReviewMode.PostImmediately,
    val postGuide: String? = null,
    val imageRef: Url? = null,
) {
    val isValid get() = !name.isNullOrBlank() && center != null

    companion object {
        val NameCharacters = setOf(' ', '.', ',', '\'', '!', '?', ':', '-', '+')
        val PathCharacters = setOf('-')

        fun isValidName(name: String) = name.all { it.isDigit() || it.isLetter() || NameCharacters.contains(it) }
                && name.length <= MAX_NAME_LENGTH
        fun isValidPath(path: String) = path.all { it.isDigit() || it.isLetter() || PathCharacters.contains(it) }
                && path.length <= MAX_NAME_LENGTH

        const val MAX_NAME_LENGTH = 32
    }
}

enum class PostPermission(label: String? = null) {
    Everyone,
    Accounts("Streetlight accounts"),
    Founder;

    val label = label ?: name
}

enum class ReviewMode(val label: String) {
    PostImmediately("User posts appear immediately"),
    PostAfterReview("User posts appear after reviewed"),
}