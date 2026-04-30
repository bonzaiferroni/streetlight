package koala.css

import kampfire.model.Url
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
        val Width = Property<String>("width", false)

        val MaskUrl = Property<UrlValue>("mask-url")
        val ColorScheme = Property<String>("color-scheme")
        val BackgroundUrl = Property<UrlValue>("background-url")
        val AnchorId = Property<PositionAnchor>("anchor-id")
        val ContainerAnchorId = Property<PositionAnchor>("anchor-container-id")
    }
}

data class InlineStyle<T>(val property: Property<T>, val value: T) {
    override fun toString() = "${property.expression}: $value"
}

data class UrlValue(val url: Url) {
    constructor(file: SiteFile): this(file.url)
    override fun toString() = "url('$url')"
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
            styles.forEach { (property, value) ->
                append(property.expression)
                append(": ")
                append(value)
                append("; ")
            }
        }
    }
}

fun CoreAttributeGroupFacade.setPositionAnchor(value: PositionAnchor) =
    setStyle(Property.PositionAnchor.to(value))

fun CoreAttributeGroupFacade.setAnchor(anchor: PositionAnchor) =
    setStyle(Property.AnchorName.to(anchor))

//fun CoreAttributeGroupFacade.setIdAndAnchor(id: Id) {
//    setId(id)
//    setAnchor(Anchor("${id.identifier}-anchor"))
//}