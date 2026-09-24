package streetlight.model.data

import kampfire.model.Url
import kotlinx.serialization.Serializable

/** A labeled link a record shows, such as to its tickets or a video. */
@Serializable
data class ExtraLink(
    val label: String,
    val url: Url,
) {
    private val urlIsValid: Boolean
        get() {
            val trimmed = url.value.trim()
            val regex = Regex(
                pattern = """^(https?)://([a-zA-Z0-9\-._~%]+)(:\d+)?(/\S*)?$"""
            )
            return regex.matches(trimmed)
        }
    /** True when the link has a label and a well-formed absolute URL. */
    val isValid get() = label.isNotBlank() && urlIsValid

    companion object {
        val Empty get () = ExtraLink("", Url(""))
    }
}