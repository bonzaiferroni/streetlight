package koala.model

import koala.css.Class

object LazyColumnStyle {
    val Scroller = Class("lazy-scroller")
    val Container = Class("lazy-container")
}

// language="CSS"
val LazyColumnCss get() = with(LazyColumnStyle) {"""

$Scroller {
    overflow-y: auto
}

$Container {
    
}

"""}