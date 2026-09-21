package koala.html

import koala.Svg
import koala.modifier.*
import kotlinx.html.DIV
import kotlinx.html.FlowContent
import kotlinx.html.div

fun FlowContent.icon(
    file: Svg,
    mod: Modifier? = modify(SmallIconHeight),
    block: DIV.() -> Unit = {}
) {
    div {
        configureIcon(
            file = file,
            mod = mod,
            block = block
        )
    }
}

fun DIV.configureIcon(
    file: Svg,
    mod: Modifier? = null,
    block: DIV.() -> Unit = {}
) {
    addModifiers(modify(IconStyle.Icon, mod))
    setStyle(Css.MaskUrl.of(file))
    block()
}

object IconStyle {
    val Icon = Class("icon")
    val Stretch = Class("icon-stretch")
    val Signal = Class("signal")

    val DefaultMod = modify(Aspect1, SmallIconHeight)
    val DefaultSignal = modify(Aspect1, SmallIconHeight, FadeIn)
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