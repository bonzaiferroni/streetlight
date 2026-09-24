package koala.html

import koala.SvgFile
import koala.modifier.*
import kotlinx.html.FlowContent

/** The Streetlight logo: the wordmark with a glowing flame. */
fun FlowContent.textLogo(
    mod: Modifier? = null
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

/** The [textLogo], linking home. */
fun FlowContent.topLogo() {
    navigation("/", AlignSelfCenter) {
        textLogo(Height(5))
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