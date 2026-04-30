package koala.html

import koala.Svg
import koala.css.*
import kotlinx.html.DIV
import kotlinx.html.FlowContent
import kotlinx.html.div

fun FlowContent.icon(
    file: Svg,
    modifiers: ModifierSet? = null,
    block: DIV.() -> Unit = {}
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
    file: Svg,
    modifiers: ModifierSet? = null,
    block: DIV.() -> Unit = {}
) {
    addModifiers(modify(IconKey.Class, modifiers))
    setStyle(Property.MaskUrl.to(UrlValue(file)))
    block()
}

object IconKey {
    val Class = Class("icon")
    val Stretch = Class("icon-stretch")
}

// language="CSS"
val IconCss get() = """
${IconKey.Class} {
    display: inline-block;
    background-color: currentColor;
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

${IconKey.Stretch} {
    mask-size: 100% 100%;   /* or any width/height ye please */
    mask-repeat: no-repeat;
}

${IconKey.Class}.clickable {
    transition: background-color var(--magic-interval) var(--magic-easing);
}

${IconKey.Class}.clickable:hover {
    background-color: rgb(var(--accent));
}

${IconKey.Class}.danger {
    background-color: rgb(var(--danger));
}
"""