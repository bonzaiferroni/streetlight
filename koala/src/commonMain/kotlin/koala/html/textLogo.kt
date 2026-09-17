package koala.html

import koala.SvgFile
import koala.modifier.AccentFg
import koala.modifier.AlignSelfCenter
import koala.modifier.Class
import koala.modifier.GlowBackground
import koala.modifier.GlowShadow
import koala.modifier.Height5
import koala.modifier.Css
import koala.modifier.ModifierSet
import koala.modifier.MoonDropShadow
import koala.modifier.PrimaryFg
import koala.modifier.modify
import koala.modifier.setAriaLabel
import koala.modifier.setStyle
import kotlinx.html.FlowContent

fun FlowContent.textLogo(
    mod: ModifierSet? = null
) {
    div(modify(mod, Class, MoonDropShadow)) {
        setAriaLabel("Streetlight Logo")
        box(modify(LogoIcon, AccentFg)) {
            setStyle(Css.MaskUrl.of(SvgFile.LogoTextStreet))
        }
        box(modify(LogoIcon, PrimaryFg)) {
            setStyle(Css.MaskUrl.of(SvgFile.LogoTextLight))
        }
        div(modify(ShadowBox, GlowShadow)) {
            div(modify(LogoIcon, GlowBackground)) {
                setStyle(Css.MaskUrl.of(SvgFile.LogoFlame))
            }
        }
    }
}

fun FlowContent.topLogo() {
    navigation("/", modify(AlignSelfCenter)) {
        textLogo(modify(Height5))
    }
}

private val Class = Class("logo")
private val ShadowBox = Class("logo-shadow-box")
private val LogoIcon = Class("logo-icon")

// language="CSS"
val LogoCss get() = """
$Class {
    display: grid;
    aspect-ratio: 4 / 1;
}

$ShadowBox {
    display: grid;
    grid-area: 1 / 1 / 2 / 2;
}

$LogoIcon {
    grid-area: 1 / 1 / 2 / 2;
    background-color: currentColor;

    mask-image: var(--mask-url);
    -webkit-mask-image: var(--mask-url);
    mask-size: contain;
    -webkit-mask-size: contain;
    mask-repeat: no-repeat;
    -webkit-mask-repeat: no-repeat;
    mask-position: center;
    -webkit-mask-position: center;
}
"""