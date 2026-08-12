package koala.model

import koala.css.Class

object GlowControlStyle {
    val Field = Class("glow-control-field")
    val Handle = Class("glow-control-handle")
}

// language="CSS"
val GlowControlCss = with(GlowControlStyle) { """
$Field {
    position: relative;
    aspect-ratio: 16 / 9;
    pointer-events: none;
    touch-action: none;
}

$Handle {
    position: absolute;
    aspect-ratio: 1;
    min-width: 1.5rem;
    transform: translate(-50%, -50%);
    border-radius: 50%;
    pointer-events: auto;
    cursor: grab;
    touch-action: none;
} 
"""}