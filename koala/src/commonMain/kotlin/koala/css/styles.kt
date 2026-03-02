package koala.css

import kotlinx.css.CssBuilder
import kotlinx.css.RuleContainer
import kotlinx.css.RuleSet
import kotlinx.html.CoreAttributeGroupFacade
import kotlinx.html.HEAD
import kotlinx.html.classes
import kotlinx.html.id
import kotlinx.html.style
import kotlin.jvm.JvmInline

interface Modifier {
    val value: String

    val selector get() = ".$value"
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

fun CoreAttributeGroupFacade.applyModifiers(modifiers: ModifierSet?) {
    modifiers?.let {
        classes += modifiers.map { it.value }
    }
}

fun CoreAttributeGroupFacade.applyModifiers(modifier: Modifier) {
    classes += modifier.value
}

fun CoreAttributeGroupFacade.applyModifiers(css: Modifier, modifiers: ModifierSet?) {
    classes += css.value
    modifiers?.let {
        classes += modifiers.map { it.value }
    }
}

fun CssBuilder.rules(theme: KoalaTheme): CssBuilder {
    // rootStyles(theme)
    // baseStyles(theme)
    // layout(theme)
    // queryLayout(theme)
    // layoutUtilities(theme)
    // utilities(theme)
    // animation(theme)
//    elementStyles(theme)
    return this
}

fun RuleContainer.rule(modifier: Modifier, block: RuleSet) = rule(".${modifier.value}", block)

fun HEAD.koalaStyles(theme: KoalaTheme = KoalaTheme()) {
    style {
        id = "koala-theme"
        +buildKoalaStyles(theme)
    }
}

fun buildKoalaStyles(theme: KoalaTheme = KoalaTheme()) = CssBuilder("    ").rules(theme).toString()

fun CssBuilder.printCss(block: () -> Unit) {
    val len = toString().length
    block()
    println(toString().substring(len))
}