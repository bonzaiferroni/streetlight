package koala.modifier

import koala.html.TagConfig
import kotlinx.css.CssBuilder
import kotlinx.html.FlowContent
import kotlinx.html.style
import kotlinx.html.unsafe

/** One change to an element: a [Class], an [InlineStyle], an [AttributeValue], or a [ModifierSet] of them. */
sealed interface Modifier

/** Modifiers applied together, in order. A `null` member is skipped, and a set may hold another set. */
class ModifierSet(val modifiers: Array<out Modifier?>): Modifier

/** This set followed by [modifiers]. */
fun ModifierSet.append(vararg modifiers: Modifier?) = modify(this, *modifiers)

/** Applies [modifiers] while the tag is built, appending to the classes and styles it already has. */
fun TagConfig.addModifiers(vararg modifiers: Modifier?) = applyModifiers(modifiers)

private fun TagConfig.applyModifiers(modifiers: Array<out Modifier?>) {
    val classBuffer = StringBuilder(attributes["class"] ?: "")
    val styleBuffer = StringBuilder(attributes["style"] ?: "")
    val classStart = classBuffer.length
    val styleStart = styleBuffer.length
    bufferModifiers(modifiers, classBuffer, styleBuffer)
    if (classBuffer.length != classStart) {
        attributes["class"] = classBuffer.toString()
    }
    if (styleBuffer.length != styleStart) {
        attributes["style"] = styleBuffer.toString()
    }
}

private fun TagConfig.bufferModifiers(
    modifiers: Array<out Modifier?>,
    classBuffer: StringBuilder,
    styleBuffer: StringBuilder,
) {
    if (styleBuffer.isNotEmpty() && styleBuffer.last() != ';') styleBuffer.append(';')
    for (modifier in modifiers) {
        when (modifier) {
            is Class -> classBuffer.appendClass(modifier)
            is InlineStyle<*> -> {
                if (styleBuffer.isNotEmpty()) styleBuffer.append(' ')
                styleBuffer.append(modifier.property.identifier)
                styleBuffer.append(": ")
                styleBuffer.append(modifier.stringValue)
                styleBuffer.append(";")
            }
            is AttributeValue<*> -> attributes[modifier.attribute.identifier] = modifier.toStringValue()
            is ModifierSet -> bufferModifiers(modifier.modifiers, classBuffer, styleBuffer)
            null -> continue
        }
    }
}

internal fun StringBuilder.appendClass(modifier: Class) {
    if (isNotEmpty()) append(' ')
    append(modifier.identifier)
}

//fun ModifierSet.append(mod: ModifierSet?) = when (mod) {
//    null -> this
//    else -> this + mod
//}
//
//fun ModifierSet.append(vararg modifiers: Modifier?) = this + modify(*modifiers)

/** A [ModifierSet] of [modifiers]. */
fun modify(vararg modifiers: Modifier?) = ModifierSet(modifiers)

//fun modify(css: Modifier, modifiers: ModifierSet?): ModifierSet {
//    val set = setOf(css)
//    return if (modifiers != null) {
//        set + modifiers
//    } else {
//        set
//    }
//}
//fun modify(modifiers: ModifierSet?, vararg additional: Modifier?) = (modifiers ?: emptySet()) + modify(*additional)
//fun modify(modifiers: ModifierSet, additional: ModifierSet?): ModifierSet {
//    return when (additional) {
//        null -> modifiers
//        else -> modifiers + additional
//    }
//}

//fun TagContext.addModifiers(modifiers: ModifierSet?) {
//    modifiers?.let {
//        classes += modifiers.map { it.identifier }
//    }
//}
//
//fun TagContext.addModifiers(vararg modifiers: Modifier?) {
//    modifiers.let {
//        classes += modifiers.mapNotNull { it?.identifier }
//    }
//}

// fun TagContext.addModifiers()

//fun TagContext.addModifiers(modifier: Modifier) {
//    classes += modifier.identifier
//}
//
//fun TagContext.addModifiers(css: Modifier, modifiers: ModifierSet?) {
//    classes += css.identifier
//    modifiers?.let {
//        classes += modifiers.map { it.identifier }
//    }
//}

//fun TagConfig.addModifiers(modifiers: ModifierSet, additional: ModifierSet?) {
//    classes += modifiers.map { it.identifier }
//    additional?.let {
//        classes += additional.map { it.identifier }
//    }
//}
//
//fun TagConfig.addModifiers(modifiers: ModifierSet?, vararg modifier: Modifier?) {
//    modifiers?.let {
//        classes += modifiers.map { it.identifier }
//    }
//    classes += modifier.mapNotNull { it?.identifier }
//}

/** Prints the CSS that [block] adds to this builder. */
fun CssBuilder.printCss(block: () -> Unit) {
    val len = toString().length
    block()
    println(toString().substring(len))
}

/** A `style` element holding [style]. */
fun FlowContent.stylesheet(style: String) {
    style {
        unsafe {
            +style
        }
    }
}