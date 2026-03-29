package koala.css

import koala.SiteFile
import kotlinx.css.Display
import kotlinx.html.CoreAttributeGroupFacade
import kotlinx.html.style

data class Property<T>(val identifier: String, val isCustom: Boolean = true) {

    fun to(value: T) = InlineStyle(this, value)

    val expression get() = when (isCustom) {
        true -> "--$identifier"
        else -> identifier
    }

    override fun toString() = expression

    companion object {
        val AnchorName = Property<PositionAnchor>("anchor-name", false)
        val PositionAnchor = Property<PositionAnchor>("position-anchor", false)
        val Display = Property<Display>("display", false)

        val MaskUrl = Property<UrlValue>("mask-url")
        val BackgroundUrl = Property<UrlValue>("background-url")
        val AnchorId = Property<PositionAnchor>("anchor-id")
        val ContainerAnchorId = Property<PositionAnchor>("anchor-container-id")
    }
}

data class InlineStyle<T>(val property: Property<T>, val value: T)

data class UrlValue(val url: String) {
    constructor(file: SiteFile): this(file.path)
    override fun toString() = "url('$url')"
}

data class Rgb(val red: Int, val green: Int, val blue: Int) {
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
    setStyle(Property.PositionAnchor.to(value))

fun CoreAttributeGroupFacade.setAnchorName(anchor: PositionAnchor) =
    setStyle(Property.AnchorName.to(anchor))