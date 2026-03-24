package koala.html

import kotlinx.html.*
import koala.css.*
import kotlinx.css.div

fun FlowContent.logo(
    modifiers: ModifierSet? = null
) {
    div {
        applyModifiers(ElementClass.logo, modifiers)
        div {
            applyStyles(styleOf(StyleProperty.maskUrl to UrlValue("/www/svg/flame.svg")))
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