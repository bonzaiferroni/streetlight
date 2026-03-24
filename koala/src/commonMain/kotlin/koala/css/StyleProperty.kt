package koala.css

import koala.SiteFile
import kotlinx.html.CoreAttributeGroupFacade
import kotlinx.html.style

data class StyleProperty<T: CssValue>(val identifier: String, val isCustom: Boolean = false) {

    fun to(value: T) = InlineStyle(this, value)

    companion object {
        val maskUrl = StyleProperty<UrlValue>("mask-url", true)
        val backgroundUrl = StyleProperty<UrlValue>("background-url", true)
        val anchorName = StyleProperty<PositionAnchorValue>("anchor-name")
        val positionAnchor = StyleProperty<PositionAnchorValue>("position-anchor")
    }
}

data class InlineStyle<T: CssValue>(val property: StyleProperty<T>, val value: CssValue)

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

typealias StyleSet = List<InlineStyle<*>>

fun styleOf(set: StyleSet?, vararg styles: InlineStyle<*>) = styles.asList().let { styles ->
    set?.let {
        styles + it
    } ?: styles
}

fun styleOf(vararg styles: InlineStyle<*>) = styles.asList()

fun CoreAttributeGroupFacade.setStyle(style: InlineStyle<*>) = setStyle(styleOf(style))

fun CoreAttributeGroupFacade.setStyle(styles: StyleSet?) {
    styles?.let {
        style = buildString {
            val style = attributes["style"]?.let { style ->
                if (style.endsWith(';')) style.dropLast(1) else style
            } ?: ""
            append(style)
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

fun CoreAttributeGroupFacade.setPositionAnchor(value: PositionAnchorValue) =
    setStyle(StyleProperty.positionAnchor.to(value))

fun CoreAttributeGroupFacade.setAnchorName(anchor: PositionAnchorValue) =
    setStyle(StyleProperty.anchorName.to(anchor))