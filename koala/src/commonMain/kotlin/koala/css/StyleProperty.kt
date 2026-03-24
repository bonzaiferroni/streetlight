package koala.css

import koala.SiteFile
import kotlinx.html.CoreAttributeGroupFacade
import kotlinx.html.style

data class StyleProperty<T: CssValue>(val identifier: String, val isCustom: Boolean = false) {

    fun to(value: T) = InlineStyle(this, value)

    companion object {
        val maskUrl = StyleProperty<UrlValue>("mask-url", true)
        val backgroundUrl = StyleProperty<UrlValue>("background-url", true)
        val anchorName = StyleProperty<PositionAnchor>("anchor-name")
        val positionAnchor = StyleProperty<PositionAnchor>("position-anchor")
        val anchorId = StyleProperty<PositionAnchor>("anchor-id", true)
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

data class PositionAnchor(val identifier: String): CssValue {
    override val expression get() = "--$identifier"
}

typealias StyleSet = List<InlineStyle<*>>

fun styleOf(set: StyleSet?, vararg styles: InlineStyle<*>) = styles.asList().let { styles ->
    set?.let {
        styles + it
    } ?: styles
}

fun styleOf(vararg styles: InlineStyle<*>) = styles.asList()

fun CoreAttributeGroupFacade.setStyle(vararg styles: InlineStyle<*>) = setStyle(styleOf(*styles))

fun CoreAttributeGroupFacade.setStyle(styles: StyleSet?) {
    styles?.let {
        style = buildString {
            attributes["style"]?.let { style ->
                append(style)
                append(' ')
            }
            styles.forEachIndexed { index, (property, value) ->
                if (property.isCustom)
                    append("--")
                append(property.identifier)
                append(": ")
                append(value.expression)
                if (index + 1 < styles.size)
                    append("; ")
                else
                    append(";")
            }
        }
    }
}

fun CoreAttributeGroupFacade.setPositionAnchor(value: PositionAnchor) =
    setStyle(StyleProperty.positionAnchor.to(value))

fun CoreAttributeGroupFacade.setAnchorName(anchor: PositionAnchor) =
    setStyle(StyleProperty.anchorName.to(anchor))