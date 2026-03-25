package koala.html

import koala.css.Css
import koala.css.ModifierSet
import koala.css.Reveal
import koala.css.StyleProperty
import koala.css.setModifiers
import koala.css.setStyle
import kotlinx.css.Display
import kotlinx.html.DIV
import kotlinx.html.FlowContent
import kotlinx.html.div

fun FlowContent.swapBlock(
    id: Id,
    modifiers: ModifierSet? = null,
    block: DIV.() -> Unit = {}
) {
    div {
        setId(id)
        setModifiers(modifiers, SwapBlockKey.Class)
        block()
    }
}

fun DIV.setReveal(isVisible: Boolean) {
    when (isVisible) {
        true -> setModifiers(Reveal)
        else -> setStyle(StyleProperty.display.to(Display.none))
    }
}

object SwapBlockKey {
    val Class = Css("swap-block")
}

// language="CSS"
val SwapBlockStyle get() = """
.swap-block {
    display: grid;
    min-width: 0;
}

.swap-block > * {
    grid-area: 1 / 1 / 2 / 2;
}

.swap-block.magic > * {
    opacity: 0;
    transition: 
            opacity var(--magic-interval) var(--magic-easing), 
            transform var(--magic-interval) var(--magic-easing), 
            filter var(--magic-interval) var(--magic-easing);
    pointer-events: none;
}

.swap-block.magic > .reveal {
    opacity: 1;
    pointer-events: auto;
}

.swap-block.magic > .slide-up {
    transform: translate(0px, 20px);
}

.swap-block.magic > .slide-up.reveal {
    transform: translate(0px, 0px);
}

.swap-block.magic > .slide-left {
    transform: translate(20px, 0px);
}

.swap-block.magic > .slide-left.reveal {
    transform: translate(0px, 0px);
}

.swap-block.magic > .slide-right {
    transform: translate(-20px, 0px);
}

.swap-block.magic > .slide-right.reveal {
    transform: translate(0px, 0px);
}

.swap-block.magic > .blur {
    filter: var(--magic-blur);
}

.swap-block.magic > .blur.reveal {
    filter: blur(0px);
}

"""
