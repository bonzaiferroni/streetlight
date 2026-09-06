package streetlight.model.data

import kampfire.api.Markdown
import kampfire.api.Slug
import kampfire.model.GeoBounds
import kampfire.model.toValidityCheck
import koala.Image
import kotlinx.serialization.Serializable

@Serializable
data class GalaxyEdit(
    val galaxyId: GalaxyId? = null,
    val cityId: CityId? = null,
    val name: String? = null,
    val slug: Slug? = null,
    val tagline: String? = null,
    val description: Markdown? = null,
    val geoBounds: GeoBounds? = null,
    val postPermission: PostPermission = PostPermission.Accounts,
    val reviewCount: Int? = 3,
    val postGuide: Markdown? = null,
    val image: Image? = null,
    val design: PageDesign? = null,
    val marks: List<FeedMark>
) {
    companion object {
        val NameCharacters = setOf(' ', '.', ',', '\'', '!', '?', ':', '-', '+')
        val PathCharacters = setOf('-')

        fun isValidName(name: String?) = name != null && isValidString(name, NameCharacters)
        fun isValidSlug(slug: Slug?) = slug != null && isValidString(slug.value, PathCharacters)

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

fun Galaxy.toEdit(marks: List<FeedMark>) = GalaxyEdit(
    galaxyId = galaxyId,
    cityId = cityId,
    name = name,
    slug = slug,
    tagline = tagline,
    description = description,
    geoBounds = geoBounds,
    postPermission = postPermission,
    reviewCount = reviewCount,
    postGuide = postGuide,
    image = image,
    design = design,
    marks = marks
)