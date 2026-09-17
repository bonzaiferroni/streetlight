package koala.modifier

import kotlinx.html.CoreAttributeGroupFacade
import kotlinx.html.style
import kotlin.random.Random

data class Property<T: Any>(
    val name: String,
    val isCustom: Boolean = false,
    val valueToString: ((T) -> String)? = null,
) {

    fun of(value: T) = InlineStyle(this, value)

    val identifier get() = when (isCustom) {
        true -> "--$name"
        else -> name
    }

    override fun toString() = identifier
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
    setStyle(Css.PositionAnchor.of(value))

fun CoreAttributeGroupFacade.setAnchorName(anchor: PositionAnchor) =
    setStyle(Css.AnchorName.of(anchor))

fun CoreAttributeGroupFacade.setRandomSeed(multiplier: Int = 1) =
    setStyle(Css.RandomSeed.of(Random.nextDouble() * multiplier))

//fun CoreAttributeGroupFacade.setIdAndAnchor(id: Id) {
//    setId(id)
//    setAnchor(Anchor("${id.identifier}-anchor"))
//}