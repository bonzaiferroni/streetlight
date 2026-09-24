package streetlight.model.data

import kampfire.api.Markdown
import kampfire.model.toValidityCheck
import koala.Image
import kotlinx.serialization.Serializable

/** The fields of a city a form sends: its name, image, description and links. */
@Serializable
data class CityEdit(
    val cityId: CityId,
    val name: String? = null,
    val image: Image? = null,
    val description: Markdown? = null,
    val links: List<ExtraLink>? = null,
) {
    val validity by lazy {
        buildSet {
            if (name.isNullOrBlank()) add(CityProperty.Name)
        }.toValidityCheck()
    }
}

/** The keys of the parts of a [CityEdit] its validity checks. */
object CityProperty {
    val Name = "name"
}

/** An edit of this city, starting from its current values. */
fun City.toEdit() = CityEdit(
    cityId = cityId,
    name = name,
    image = image,
    description = description,
    links = links,
)
