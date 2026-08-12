package koala.model

import koala.css.Class

object LightControl {
    val Field = Class("position-field")
    val Handle = Class("position-handle")
}

// language="CSS"
val LightControlCss = with(LightControl) { """
$Field {
    position: relative;
    aspect-ratio: 16 / 9;
    pointer-events: none;
    touch-action: none;
}

$Handle {
    position: absolute;
    aspect-ratio: 1;
    transform: translate(-50%, -50%);
    border-radius: 50%;
    pointer-events: auto;
    cursor: grab;
    touch-action: none;
} 
"""}