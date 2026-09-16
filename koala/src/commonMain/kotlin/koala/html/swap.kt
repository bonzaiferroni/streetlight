package koala.html

import koala.modifier.Class
import koala.modifier.Reveal

class swap {
}

object SwapStyle {
    val Class = Class("swap")
}

//language="CSS"
val SwapCss get() = with(SwapStyle) { """

$Class {
    transition: var(--transition-height);

    > * {
        display: none;
    }
    
    > $Reveal {
        display: block;
        opacity: 1;
        transition: var(--transition-opacity);
    
        @starting-style {
            opacity: 0;
        }
    }
}

"""}