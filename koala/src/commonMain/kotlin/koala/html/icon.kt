package koala.html

import koala.SvgFile
import koala.css.*
import kotlinx.html.DIV
import kotlinx.html.FlowContent
import kotlinx.html.div

fun FlowContent.icon(
    file: SvgFile,
    modifiers: ModifierSet? = null,
    block: (DIV.() -> Unit)? = null
) {
    div {
        configureIcon(
            file = file,
            modifiers = modifiers,
            block = block
        )
    }
}

fun DIV.configureIcon(
    file: SvgFile,
    modifiers: ModifierSet? = null,
    block: (DIV.() -> Unit)? = null
) {
    setModifiers(modify(IconElement.cssClass, modifiers))
    setStyle(StyleProperty.maskUrl.to(UrlValue(file.path)))
    block?.invoke(this)
}

object IconElement {
    val cssClass = Css("icon")
}

// language="CSS"
const val ICON_STYLES = """
.icon {
    display: inline-block;
    background-color: currentColor;
    height: 2rem;
    aspect-ratio: 1 / 1;

    mask-image: var(--mask-url);
    -webkit-mask-image: var(--mask-url);

    mask-size: contain;
    -webkit-mask-size: contain;

    mask-repeat: no-repeat;
    -webkit-mask-repeat: no-repeat;

    mask-position: center;
    -webkit-mask-position: center;
}

.icon.clickable {
    transition: background-color 200ms ease-in-out;
}

.icon.clickable:hover {
    background-color: rgb(var(--accent));
}

.icon.danger {
    background-color: rgb(var(--danger));
}
"""