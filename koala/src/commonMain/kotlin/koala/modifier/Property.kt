package koala.modifier

import kotlinx.html.CoreAttributeGroupFacade
import kotlinx.html.style
import kotlin.random.Random

/** A CSS property, standard or custom. A custom property is written with a `--` prefix. */
data class Property<T: Any>(
    val name: String,
    val isCustom: Boolean = false,
    val valueToString: ((T) -> String)? = null,
) {

    /** An [InlineStyle] setting this property to [value]. */
    fun of(value: T) = InlineStyle(this, value)

    val identifier get() = when (isCustom) {
        true -> "--$name"
        else -> name
    }

    override fun toString() = identifier
}

/** The name of a CSS anchor that a positioned element attaches to. */
data class PositionAnchor(val identifier: String) {
    override fun toString() = "--$identifier"
}

/** Inline styles applied together. A `null` member is skipped. */
typealias StyleSet = List<InlineStyle<*>?>

/** [styles] followed by those of [set]. */
fun styleOf(set: StyleSet?, vararg styles: InlineStyle<*>) = styles.asList().let { styles ->
    set?.let {
        styles + it
    } ?: styles
}

/** A [StyleSet] of [styles]. */
fun styleOf(vararg styles: InlineStyle<*>?) = styles.asList()

/** Appends [styles] to the tag's inline style. */
fun CoreAttributeGroupFacade.setStyle(vararg styles: InlineStyle<*>?) = setStyle(styleOf(*styles))

/** Appends [styles] to the tag's inline style. */
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

/** Positions the element against the anchor [value]. */
fun CoreAttributeGroupFacade.setPositionAnchor(value: PositionAnchor) =
    setStyle(Css.PositionAnchor.of(value))

/** Names the element as the anchor [anchor]. */
fun CoreAttributeGroupFacade.setAnchorName(anchor: PositionAnchor) =
    setStyle(Css.AnchorName.of(anchor))

/** Sets a random seed on the element, up to [multiplier], for CSS that varies each element slightly. */
fun CoreAttributeGroupFacade.setRandomSeed(multiplier: Int = 1) =
    setStyle(Css.RandomSeed.of(Random.nextDouble() * multiplier))

//fun CoreAttributeGroupFacade.setIdAndAnchor(id: Id) {
//    setId(id)
//    setAnchor(Anchor("${id.identifier}-anchor"))
//}