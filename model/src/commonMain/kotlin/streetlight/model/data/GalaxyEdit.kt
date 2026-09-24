package streetlight.model.data

import kampfire.api.Markdown
import kampfire.api.Slug
import kampfire.model.GeoRect
import kampfire.model.toValidityCheck
import koala.Image
import kotlinx.serialization.Serializable

/** The fields of a galaxy a form sends. */
@Serializable
data class GalaxyEdit(
    val galaxyId: GalaxyId? = null,
    val cityId: CityId? = null,
    val name: String? = null,
    val slug: Slug? = null,
    val tagline: String? = null,
    val description: Markdown? = null,
    val geoRect: GeoRect? = null,
    val postPermission: PostPermission = PostPermission.Accounts,
    val reviewCount: Int? = 3,
    val postGuide: Markdown? = null,
    val image: Image? = null,
    val design: PageDesign? = null,
    val marks: List<GalaxyMark>
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

/** The keys of the parts of a [GalaxyEdit] its validity checks. */
object GalaxyProperty {
    val Name = "name"
    val Path = "path"
}

/** An edit of this galaxy, starting from its current values and [marks]. */
fun Galaxy.toEdit(marks: List<GalaxyMark>) = GalaxyEdit(
    galaxyId = galaxyId,
    cityId = cityId,
    name = name,
    slug = slug,
    tagline = tagline,
    description = description,
    geoRect = geoRect,
    postPermission = postPermission,
    reviewCount = reviewCount,
    postGuide = postGuide,
    image = image,
    design = design,
    marks = marks
)