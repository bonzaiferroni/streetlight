package koala.css

import kotlin.jvm.JvmInline

@JvmInline
value class CustomProperty(val identifier: String)

object CustomProperties {
    val maskSrc = CustomProperty("mask-src")
}

interface CssValue {
    val value: String
}

data class UrlValue(val url: String): CssValue {
    override val value get() = "url('$url')"
}

fun styleOf(property: CustomProperty, value: CssValue) = "--${property.identifier}: ${value.value};"