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

"""}