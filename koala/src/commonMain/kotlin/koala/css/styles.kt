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

interface CssClass {
    val value: String
}

@JvmInline
value class Css(override val value: String): CssClass {
    override fun toString() = value
}

typealias ModifierSet = Set<CssClass>

fun modify(vararg cssClass: CssClass) = cssClass.toSet()
fun modify(css: CssClass, modifiers: ModifierSet?): ModifierSet {
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

fun CoreAttributeGroupFacade.applyModifiers(cssClass: CssClass) {
    classes += cssClass.value
}

fun CoreAttributeGroupFacade.applyModifiers(css: CssClass, modifiers: ModifierSet?) {
    classes += css.value
    modifiers?.let {
        classes += modifiers.map { it.value }
    }
}

fun CssBuilder.rules(theme: KoalaTheme): CssBuilder {
    rootStyles(theme)
    baseStyles(theme)
    layout(theme)
    queryLayout(theme)
    layoutUtilities(theme)
    utilities(theme)
    animation(theme)
    elementStyles(theme)
    return this
}

fun RuleContainer.rule(cssClass: CssClass, block: RuleSet) = rule(".${cssClass.value}", block)

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