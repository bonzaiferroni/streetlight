package koala.html

import koala.SvgFile
import kotlinx.html.*
import koala.css.*

fun FlowContent.logoProto(
    modifiers: ModifierSet? = null
) {
    div {
        addModifiers(LogoProtoKey.Class, modifiers)
        div {
            setStyle(Property.MaskUrl.to(UrlValue(SvgFile.Flame)))
        }
    }
}

object LogoProtoKey {
    val Class = Class("logo")
}

// language="CSS"
val LogoProtoCss get() = """
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