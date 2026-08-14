package koala.css

import kampfire.model.Url
import koala.Asset
import kotlinx.serialization.Serializable

fun styleValueOf(value: Any) = when (value) {
    is Url -> "url('${value.value}')"
    is Asset -> "url('${value.url.value}')"
    else -> value.toString()
}

data class UrlValue(val url: Url) {
    constructor(file: Asset): this(file.url)
    override fun toString() = "url('$url')"
}

