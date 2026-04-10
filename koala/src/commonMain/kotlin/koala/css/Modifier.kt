package koala.css

import koala.html.Queryable
import koala.html.TagContext
import kotlinx.css.CssBuilder
import kotlinx.css.RuleContainer
import kotlinx.css.RuleSet
import kotlinx.html.FlowContent
import kotlinx.html.classes
import kotlinx.html.style
import kotlinx.html.unsafe
import kotlin.jvm.JvmInline

interface Modifier: Queryable {
    val identifier: String

    override val selector get() = ".$identifier"
}

@JvmInline
value class Class(override val identifier: String): Modifier {
    override fun toString() = selector
}

typealias ModifierSet = Set<Modifier>

fun modify(vararg modifiers: Modifier) = modifiers.toSet()
fun modify(css: Modifier, modifiers: ModifierSet?): ModifierSet {
    val set = setOf(css)
    return if (modifiers != null) {
        set + modifiers
    } else {
        set
    }
}
fun modify(modifiers: ModifierSet?, vararg additional: Modifier) = (modifiers ?: emptySet()) + additional.toSet()
fun modify(modifiers: ModifierSet, additional: ModifierSet?): ModifierSet {
    return when (additional) {
        null -> modifiers
        else -> modifiers + additional
    }
}

fun TagContext.addModifiers(modifiers: ModifierSet?) {
    modifiers?.let {
        classes += modifiers.map { it.identifier }
    }
}

fun TagContext.addModifiers(modifier: Modifier) {
    classes += modifier.identifier
}

fun TagContext.addModifiers(css: Modifier, modifiers: ModifierSet?) {
    classes += css.identifier
    modifiers?.let {
        classes += modifiers.map { it.identifier }
    }
}

fun TagContext.addModifiers(modifiers: ModifierSet?, vararg modifier: Modifier?) {
    modifiers?.let {
        classes += modifiers.map { it.identifier }
    }
    classes += modifier.mapNotNull { it?.identifier }
}

fun RuleContainer.rule(modifier: Modifier, block: RuleSet) = rule(".${modifier.identifier}", block)

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