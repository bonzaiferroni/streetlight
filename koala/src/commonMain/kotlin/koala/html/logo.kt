package koala.html

import koala.SvgFiles
import kotlinx.html.*
import koala.css.*

fun FlowContent.logo(
    modifiers: ModifierSet? = null
) {
    div {
        setModifiers(ElementClass.logo, modifiers)
        div {
            setStyle(StyleProperty.maskUrl.to(UrlValue(SvgFiles.flame)))
        }
    }
}

// language="CSS"
const val LOGO_STYLES = """
.logo {
    display: flex;
    align-items: center;
    justify-content: center;
    animation: glow-shadow 10s infinite linear;
}

.logo > div {
    height: 100%;
    background-color: currentColor;
    aspect-ratio: 2 / 3;

    display: block;

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