package koala.css

import kotlinx.css.*
import kotlinx.css.properties.Animation
import kotlinx.css.properties.AnimationDirection
import kotlinx.css.properties.Animations
import kotlinx.css.properties.FillMode
import kotlinx.css.properties.IterationCount
import kotlinx.css.properties.PlayState
import kotlinx.css.properties.Timing
import kotlinx.css.properties.s

fun CssBuilder.utilities(theme: KoalaTheme) {
    classRule(DisplayNone) {
        put("display", "none !important")
    }

    classRule(Bold) {
        fontWeight = FontWeight.bold
    }

    classRule(Italic) {
        fontStyle = FontStyle.italic
    }

    classRule(NoGap) {
        put("gap", "0 !important")
    }

    classRule(AlignItemsCenter) {
        alignItems = Align.center
    }

    classRule(AlignItemsStretch) {
        alignItems = Align.stretch
    }

    classRule(FillWidth) {
        width = 100.pct
    }

    classRule(Dim) {
        color = theme.fg.changeAlpha(0.6)
    }

    classRule(NoDim) {
        put("color", "${theme.fg} !important")
    }

    classRule(Glow) {
        animation += Animation("glow", 10.s, iterationCount = IterationCount.infinite, timing = Timing.linear)
    }

    classRule(Flex1) {
        flex = Flex(1)
    }

    classRule(Flex2) {
        flex = Flex(2)
    }

    classRule(Flex3) {
        flex = Flex(3)
    }

    classRule(Flex4) {
        flex = Flex(4)
    }

    "p.${Large.value}" {
        fontSize = 1.4.rem
    }

    classRule(FlexItems1) {
        children {
            flex = Flex(1)
        }
    }

    classRule(TextAlignCenter) {
        textAlign = TextAlign.center
    }

    classRule(TextAlignRight) {
        textAlign = TextAlign.right
    }
}