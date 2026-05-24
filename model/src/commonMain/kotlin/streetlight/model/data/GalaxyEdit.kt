package streetlight.model.data

import kampfire.api.Slug
import kampfire.model.GeoBounds
import kampfire.model.Url
import kampfire.model.toValidityCheck
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

        fun isValidName(name: String?) = name != null && isValidString(name, NameCharacters)
        fun isValidSlug(slug: Slug?) = slug != null && isValidString(slug.string, PathCharacters)

        private fun isValidString(path: String, validCharacters: Set<Char>) =
            path.isNotBlank() && path.all { it.isDigit() || it.isLetter() || validCharacters.contains(it) }
                && path.length <= Slug.MAX_LENGTH
    }

    val validity by lazy {
        buildSet {
            if (!isValidName(name)) add(GalaxyProperty.Name)
            if (!isValidSlug(slug)) add(GalaxyProperty.Path)
        }.toValidityCheck()
    }
}

object GalaxyProperty {
    val Name = "name"
    val Path = "path"
}

fun Galaxy.toEdit() = GalaxyEdit(
    galaxyId = galaxyId,
    cityId = cityId,
    name = name,
    slug = slug,
    tagline = tagline,
    description = description,
    geoBounds = geoBounds,
    postPermission = postPermission,
    reviewMode = reviewMode,
    postGuide = postGuide,
    imageRef = imageRef,
)