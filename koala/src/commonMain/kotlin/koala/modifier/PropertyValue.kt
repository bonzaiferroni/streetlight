package koala.modifier

import kampfire.model.Url
import koala.Asset

/** [value] as CSS text. A [Url] or an [Asset] is written as `url(...)`. */
fun styleValueOf(value: Any) = when (value) {
    is Url -> "url('${value.value}')"
    is Asset -> "url('${value.url.value}')"
    else -> value.toString()
}

/** A URL as a CSS `url(...)` value. */
data class UrlValue(val url: Url) {
    constructor(file: Asset): this(file.url)
    override fun toString() = "url('$url')"
}

