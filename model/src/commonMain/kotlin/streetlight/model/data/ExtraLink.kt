package streetlight.model.data

import kampfire.model.Url
import kotlinx.serialization.Serializable

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
    val isValid get() = label.isNotBlank() && urlIsValid

    companion object {
        val Blank get () = ExtraLink("", Url(""))
    }
}