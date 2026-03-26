package koala.css

import koala.html.Queryable
import koala.html.TagContext
import kotlinx.css.CssBuilder
import kotlinx.css.RuleContainer
import kotlinx.css.RuleSet
import kotlinx.html.classes
import kotlin.jvm.JvmInline

interface Modifier: Queryable {
    val value: String

    override val selector get() = ".$value"
}

@JvmInline
value class Css(override val value: String): Modifier {
    override fun toString() = value
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

fun TagContext.addModifiers(modifiers: ModifierSet?) {
    modifiers?.let {
        classes += modifiers.map { it.value }
    }
}

fun TagContext.addModifiers(modifier: Modifier) {
    classes += modifier.value
}

fun TagContext.addModifiers(css: Modifier, modifiers: ModifierSet?) {
    classes += css.value
    modifiers?.let {
        classes += modifiers.map { it.value }
    }
}

fun TagContext.addModifiers(modifiers: ModifierSet?, vararg modifier: Modifier?) {
    modifiers?.let {
        classes += modifiers.map { it.value }
    }
    classes += modifier.mapNotNull { it?.value }
}

fun RuleContainer.rule(modifier: Modifier, block: RuleSet) = rule(".${modifier.value}", block)

fun CssBuilder.printCss(block: () -> Unit) {
    val len = toString().length
    block()
    println(toString().substring(len))
}