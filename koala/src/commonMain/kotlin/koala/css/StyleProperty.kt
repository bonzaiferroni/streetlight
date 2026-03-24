package koala.css

import koala.SiteFile
import kotlinx.html.CoreAttributeGroupFacade
import kotlinx.html.style
import kotlin.jvm.JvmInline

data class StyleProperty(val identifier: String, val isCustom: Boolean = false) {
    companion object {
        val maskUrl = StyleProperty("mask-url", true)
        val backgroundUrl = StyleProperty("background-url", true)
        val anchorName = StyleProperty("anchor-name")
        val positionAnchor = StyleProperty("position-anchor")
    }
}

interface CssValue {
    val expression: String
}

data class UrlValue(val url: String): CssValue {
    constructor(file: SiteFile): this(file.path)

    override val expression get() = "url('$url')"
}

data class RgbValue(val red: Int, val green: Int, val blue: Int): CssValue {
    override val expression get() = "$red, $green, $blue"
}

data class PositionAnchorValue(val identifier: String): CssValue {
    override val expression get() = "--$identifier"
}

fun styleOf(set: StyleSet?, vararg styles: Pair<StyleProperty, CssValue>) = styles.asList().let { styles ->
    set?.let {
        styles + it
    } ?: styles
}

fun styleOf(vararg styles: Pair<StyleProperty, CssValue>) = styles.asList()

typealias StyleSet = List<Pair<StyleProperty, CssValue>>

fun CoreAttributeGroupFacade.applyStyles(styles: StyleSet?) {
    styles?.let {
        style = buildString {
            styles.forEachIndexed { index, (property, value) ->
                if (property.isCustom)
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