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

fun CoreAttributeGroupFacade.modify(vararg cssClass: CssClass?) {
    classes = cssClass.mapNotNull { it?.value }.toSet()
}

fun CssBuilder.rules(theme: KoalaTheme): CssBuilder {
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

fun buildKoalaStyles(theme: KoalaTheme = KoalaTheme()) = CssBuilder().rules(theme).toString()