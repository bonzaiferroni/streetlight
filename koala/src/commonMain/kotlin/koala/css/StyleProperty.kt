package koala.css

import kotlinx.html.CoreAttributeGroupFacade
import kotlinx.html.style
import kotlin.jvm.JvmInline

@JvmInline
value class StyleProperty(val identifier: String) {
    companion object {
        val maskSrc = StyleProperty("mask-url")
        val backgroundUrl = StyleProperty("background-url")
    }
}

interface CssValue {
    val expression: String
}

data class UrlValue(val url: String): CssValue {
    override val expression get() = "url('$url')"
}

data class RgbValue(val red: Int, val green: Int, val blue: Int): CssValue {
    override val expression get() = "$red, $green, $blue"
}

fun styleOf(vararg styles: Pair<StyleProperty, CssValue>) = styles.asList()

typealias StyleSet = List<Pair<StyleProperty, CssValue>>

fun CoreAttributeGroupFacade.applyStyles(styles: StyleSet?) {
    styles?.let {
        style = buildString {
            styles.forEachIndexed { index, (property, value) ->
                append("--")
                append(property.identifier)
                append(": ")
                append(value.expression)
                if (index + 1 < styles.size)
                    append(", ")
                else
                    append(";")
            }
        }
    }
}