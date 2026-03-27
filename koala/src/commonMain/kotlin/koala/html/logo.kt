package koala.html

import koala.SvgFile
import kotlinx.html.*
import koala.css.*

fun FlowContent.logo(
    modifiers: ModifierSet? = null
) {
    div {
        addModifiers(LogoKey.Class, modifiers)
        div {
            setStyle(StyleProperty.maskUrl.to(UrlValue(SvgFile.Flame)))
        }
    }
}

object LogoKey {
    val Class = Css("logo")
}

// language="CSS"
val LogoCss get() = """
.logo {
    display: flex;
    height: 100%;
    animation: glow-shadow 10s infinite linear;
}

.logo > div {
    height: 100%;
    aspect-ratio: 2 / 3;
    display: block;
    background-color: currentColor;

    mask-image: var(--mask-url);
    -webkit-mask-image: var(--mask-url);
    mask-size: contain;
    -webkit-mask-size: contain;
    mask-repeat: no-repeat;
    -webkit-mask-repeat: no-repeat;
    mask-position: center;
    -webkit-mask-position: center;

    animation: glow-background 10s infinite linear;
}
"""