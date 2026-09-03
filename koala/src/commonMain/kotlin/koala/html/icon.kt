package koala.html

import koala.Svg
import koala.css.*
import kotlinx.html.DIV
import kotlinx.html.FlowContent
import kotlinx.html.div

fun FlowContent.icon(
    file: Svg,
    modifiers: ModifierSet? = modify(Height3),
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
    addModifiers(modify(IconStyle.Icon, modifiers))
    setStyle(Property.MaskUrl.to(file))
    block()
}

object IconStyle {
    val Icon = Class("icon")
    val Stretch = Class("icon-stretch")
    val Signal = Class("signal")

    val DefaultMod = modify(Aspect1, Height3)
    val DefaultSignal = modify(Aspect1, Height3, FadeIn)
}

// language="CSS"
val IconCss get() = with(IconStyle) { """
$Icon {
    display: inline-block;
    background-color: currentColor;
    aspect-ratio: 1 / 1;

    mask-image: var(--mask-url);
    -webkit-mask-image: var(--mask-url);

    mask-repeat: no-repeat;
    -webkit-mask-repeat: no-repeat;

    mask-position: center;
    -webkit-mask-position: center;
    
    mask-size: 100% 100%;
    -webkit-mask-size: 100% 100%;
}

$Stretch {
    mask-size: 100% 100%;   /* or any width/height ye please */
    mask-repeat: no-repeat;
}

$Icon$Clickable {
    /*  transition: background-color var(--magic-interval) var(--magic-easing); */
}

$Icon$Clickable:hover {
    background-color: rgb(var(--accent));
}

$Icon$Danger {
    background-color: rgb(var(--danger));
}
""" }