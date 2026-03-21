package koala.css

import kotlin.jvm.JvmInline

@JvmInline
value class StyleProperty(val identifier: String)

object CustomProperty {
    val maskSrc = StyleProperty("mask-src")
}

interface CssValue {
    val value: String
}

data class UrlValue(val url: String): CssValue {
    override val value get() = "url('$url')"
}

data class RgbValue(val red: Int, val green: Int, val blue: Int): CssValue {
    override val value get() = "$red, $green, $blue"
}

fun styleOf(property: StyleProperty, value: CssValue) = "--${property.identifier}: ${value.value};"