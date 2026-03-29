package koala.css

import koala.SiteFile
import kotlinx.css.Display
import kotlinx.html.CoreAttributeGroupFacade
import kotlinx.html.style

data class StyleProperty<T>(val identifier: String, val isCustom: Boolean = false) {

    fun to(value: T) = InlineStyle(this, value)

    val expression get() = when (isCustom) {
        true -> "--$identifier"
        else -> identifier
    }

    override fun toString() = expression

    companion object {
        val anchorName = StyleProperty<PositionAnchor>("anchor-name")
        val positionAnchor = StyleProperty<PositionAnchor>("position-anchor")
        val display = StyleProperty<Display>("display")

        val maskUrl = StyleProperty<UrlValue>("mask-url", true)
        val backgroundUrl = StyleProperty<UrlValue>("background-url", true)
        val anchorId = StyleProperty<PositionAnchor>("anchor-id", true)
        val containerAnchorId = StyleProperty<PositionAnchor>("anchor-container-id", true)
    }
}

data class InlineStyle<T>(val property: StyleProperty<T>, val value: T)

data class UrlValue(val url: String) {
    constructor(file: SiteFile): this(file.path)
    override fun toString() = "url('$url')"
}

data class RgbValue(val red: Int, val green: Int, val blue: Int) {
    override fun toString() = "$red, $green, $blue"
}

data class PositionAnchor(val identifier: String) {
    override fun toString() = "--$identifier"
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
                append(property.expression)
                append(": ")
                append(value)
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