package koala.modifier

import koala.html.TagConfig
import kotlinx.css.CssBuilder
import kotlinx.html.FlowContent
import kotlinx.html.style
import kotlinx.html.unsafe

sealed interface Modifier {
    val unmodifier: Unmodifier?
}

sealed interface Unmodifier {
    val identifier: String
}

class ModifierSet(val modifiers: Array<out Modifier?>): Modifier {
    override val unmodifier get() = null
}

fun ModifierSet.append(vararg modifiers: Modifier?) = modify(this, *modifiers)

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
            is ClassModifier -> classBuffer.appendClass(modifier)
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

internal fun StringBuilder.appendClass(modifier: ClassModifier) {
    if (isNotEmpty()) append(' ')
    append(modifier.identifier)
}

//fun ModifierSet.append(mod: ModifierSet?) = when (mod) {
//    null -> this
//    else -> this + mod
//}
//
//fun ModifierSet.append(vararg modifiers: Modifier?) = this + modify(*modifiers)

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

fun CssBuilder.printCss(block: () -> Unit) {
    val len = toString().length
    block()
    println(toString().substring(len))
}

fun FlowContent.stylesheet(style: String) {
    style {
        unsafe {
            +style
        }
    }
}