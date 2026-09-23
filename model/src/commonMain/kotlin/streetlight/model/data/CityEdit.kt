package streetlight.model.data

import kampfire.api.Markdown
import kampfire.model.toValidityCheck
import koala.Image
import kotlinx.serialization.Serializable

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

object CityProperty {
    val Name = "name"
}

fun City.toEdit() = CityEdit(
    cityId = cityId,
    name = name,
    image = image,
    description = description,
    links = links,
)
