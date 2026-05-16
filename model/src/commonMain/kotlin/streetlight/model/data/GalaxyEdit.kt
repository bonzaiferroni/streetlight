package streetlight.model.data

import kampfire.model.GeoBounds
import kampfire.model.GeoPoint
import kampfire.model.Url
import kotlinx.serialization.Serializable

@Serializable
data class GalaxyEdit(
    val galaxyId: GalaxyId? = null,
    val cityId: CityId? = null,
    val name: String? = null,
    val slug: Slug? = null,
    val tagline: String? = null,
    val description: String? = null,
    val geoBounds: GeoBounds? = null,
    val postPermission: PostPermission = PostPermission.Accounts,
    val reviewMode: ReviewMode = ReviewMode.PostImmediately,
    val postGuide: String? = null,
    val imageRef: Url? = null,
) {
    companion object {
        val NameCharacters = setOf(' ', '.', ',', '\'', '!', '?', ':', '-', '+')
        val PathCharacters = setOf('-')

        fun isValidName(name: String?) = !name.isNullOrBlank()
                && name.all { it.isDigit() || it.isLetter() || NameCharacters.contains(it) }
                && name.length <= MAX_NAME_LENGTH
        fun isValidPath(path: String?) = !path.isNullOrBlank()
                && path.all { it.isDigit() || it.isLetter() || PathCharacters.contains(it) }
                && path.length <= MAX_NAME_LENGTH

        const val MAX_NAME_LENGTH = 36
    }

    val invalidParts by lazy {
        buildSet {
            if (!isValidName(name)) add(GalaxyProperty.Name)
            if (!isValidPath(slug)) add(GalaxyProperty.Path)
        }
    }

    val invalidMessage get() = invalidParts.firstOrNull()?.let { "missing: $it"}

    val isValid get() = invalidParts.isEmpty()
}

object GalaxyProperty {
    val Name = "name"
    val Path = "path"
}