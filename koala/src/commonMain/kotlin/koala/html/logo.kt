package koala.html

import koala.SvgFile
import koala.css.AntiShadow
import koala.css.Class
import koala.css.GlowBackground
import koala.css.GlowShadow
import koala.css.ModifierSet
import koala.css.Property
import koala.css.UrlValue
import koala.css.modify
import koala.css.setStyle
import kotlinx.html.FlowContent

fun FlowContent.logo(
    modifiers: ModifierSet? = null
) {
    div(modify(modifiers, Class, AntiShadow)) {
        div(modify(LogoIcon)) {
            setStyle(Property.MaskUrl.to(UrlValue(SvgFile.LogoText)))
        }
        div(modify(ShadowBox, GlowShadow)) {
            div(modify(LogoIcon, GlowBackground)) {
                setStyle(Property.MaskUrl.to(UrlValue(SvgFile.LogoFlame)))
            }
        }
    }
}

private val Class = Class("logo")
private val ShadowBox = Class("logo-shadow-box")
private val LogoIcon = Class("logo-icon")

// language="CSS"
val LogoCss get() = """
${Class} {
    display: grid;
    aspect-ratio: 4 / 1;
    width: 168px;
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