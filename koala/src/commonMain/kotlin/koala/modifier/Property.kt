package koala.modifier

import kampfire.model.Url
import koala.Asset
import kotlinx.css.Display
import kotlinx.css.GridTemplateColumns
import kotlinx.css.LinearDimension
import kotlinx.html.CoreAttributeGroupFacade
import kotlinx.html.style
import kotlin.random.Random

data class Property<T: Any>(
    val name: String,
    val isCustom: Boolean = false,
    val valueToString: ((T) -> String)? = null,
): Unmodifier {

    fun to(value: T) = InlineStyle(this, value)

    override val identifier get() = when (isCustom) {
        true -> "--$name"
        else -> name
    }

    override fun toString() = identifier

    companion object {
        val AnchorName = Property<PositionAnchor>("anchor-name")
        val PositionAnchor = Property<PositionAnchor>("position-anchor")
        val Display = Property<Display>("display")
        val Width = Property<LinearDimension>("width")
        val Height = Property<LinearDimension>("height")
        val ZIndex = Property<Int>("z-index")
        val GridTemplateColumns = Property<GridTemplateColumns>("grid-template-columns")
        val ViewTransitionName = Property<String>("view-transition-name")
        val AspectRatio = Property<Float>("aspect-ratio")
        val BackgroundColor = Property<String>("background-color")
        val Top = Property<LinearDimension>("top")
        val Left = Property<LinearDimension>("left")
        val Margin = Property<String>("margin")
        val BackgroundImage = Property<String>("background-image")

        val MaskUrl = Property<Asset>("mask-url", true)
        val ColorScheme = Property<String>("color-scheme", true)
        val BackgroundUrl = Property<Url>("background-url", true)
        val AnchorId = Property<PositionAnchor>("anchor-id", true)
        val ContainerAnchorId = Property<PositionAnchor>("anchor-container-id", true)
        val ColumnCount = Property<Int>("column-count", true)
        val InlineImage = Property<Url>("inline-image", true)
        val RandomSeed = Property<Number>("random-seed", true)
    }
}

data class PositionAnchor(val identifier: String) {
    override fun toString() = "--$identifier"
}

typealias StyleSet = List<InlineStyle<*>?>

fun styleOf(set: StyleSet?, vararg styles: InlineStyle<*>) = styles.asList().let { styles ->
    set?.let {
        styles + it
    } ?: styles
}

fun styleOf(vararg styles: InlineStyle<*>?) = styles.asList()

fun CoreAttributeGroupFacade.setStyle(vararg styles: InlineStyle<*>?) = setStyle(styleOf(*styles))

fun CoreAttributeGroupFacade.setStyle(styles: StyleSet) {
    styles.let {
        style = buildString {
            attributes["style"]?.let { style ->
                append(style)
                append(' ')
            }
            styles.forEach { style ->
                if (style == null) return@forEach
                append(style.property.identifier)
                append(": ")
                append(style.stringValue)
                append("; ")
            }
        }
    }
}

fun CoreAttributeGroupFacade.setPositionAnchor(value: PositionAnchor) =
    setStyle(Property.PositionAnchor.to(value))

fun CoreAttributeGroupFacade.setAnchorName(anchor: PositionAnchor) =
    setStyle(Property.AnchorName.to(anchor))

fun CoreAttributeGroupFacade.setRandomSeed(multiplier: Int = 1) =
    setStyle(Property.RandomSeed.to(Random.nextDouble() * multiplier))

//fun CoreAttributeGroupFacade.setIdAndAnchor(id: Id) {
//    setId(id)
//    setAnchor(Anchor("${id.identifier}-anchor"))
//}