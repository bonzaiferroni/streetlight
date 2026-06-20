package koala.html

import koala.css.Class
import koala.css.Reveal

class swap {
}

object SwapStyle {
    val Class = Class("swap")
}

//language="CSS"
val SwapCss get() = with(SwapStyle) { """

$Class {
    > * {
        display: none;
    }
    
    > $Reveal {
        display: block;
    }
}

::view-transition-old(swap) {
    animation: slide-out 0.3s ease;
}

::view-transition-new(swap) {
    animation: slide-in 0.3s ease;
}

@keyframes slide-out {
    to { transform: translateX(-100%); opacity: 0; }
}

@keyframes slide-in {
    from { transform: translateX(100%); opacity: 0; }
}

"""}