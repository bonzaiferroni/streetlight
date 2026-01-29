package koala.css

import kotlinx.css.CssBuilder
import kotlinx.css.Display
import kotlinx.css.FlexDirection
import kotlinx.css.LinearDimension
import kotlinx.css.Rule
import kotlinx.css.RuleContainer
import kotlinx.css.RuleSet
import kotlinx.css.display
import kotlinx.css.flexDirection
import kotlinx.css.gap
import kotlinx.css.rem
import kotlinx.html.CoreAttributeGroupFacade
import kotlinx.html.classes
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

// layouts
object Column : CssClass { override val value = "layout-column" }
object Row : CssClass { override val value = "layout-row" }
object Card : CssClass { override val value = "layout-card" }
object Box : CssClass { override val value = "layout-box"}

// utilities
object DisplayNone : CssClass { override val value = "display-none" }
object Bold: CssClass { override val value = "bold" }
object Italic: CssClass { override val value = "italic" }
object NoGap: CssClass { override val value = "no-gap" }
object AlignItemsCenter: CssClass { override val value = "align-items-center" }
object AlignItemsStretch: CssClass { override val value = "align-items-stretch" }
object FillWidth: CssClass { override val value = "fill-width" }
object Dim: CssClass { override val value = "dim" }
object NoDim: CssClass { override val value = "no-dim" }
object Glow: CssClass { override val value = "glow" }
object Flex1: CssClass { override val value = "flex-1" }
object Flex2: CssClass { override val value = "flex-2" }
object Flex3: CssClass { override val value = "flex-3" }
object Flex4: CssClass { override val value = "flex-4" }
object Large: CssClass { override val value = "large" }
object FlexItems1: CssClass { override val value = "flex-items-1" }
object TextAlignCenter: CssClass { override val value = "text-align-center"}
object TextAlignRight: CssClass { override val value = "text-align-right"}

// animation
object Fade: CssClass { override val value = "fade" }
object Show: CssClass { override val value = "show" }
object FadeStack: CssClass { override val value = "fade-stack" }

fun CssBuilder.rules(theme: KoalaTheme): CssBuilder {
    layout(theme)
    utilities(theme)
    animation(theme)
    return this
}

fun RuleContainer.classRule(cssClass: CssClass, block: RuleSet) = rule(".${cssClass.value}", block)