package koala.model

import koala.css.Class

object LazyColumnStyle {
    val Container = Class("lazy-column")
}

// language="CSS"
val LazyColumnCss get() = with(LazyColumnStyle) {"""

$Container {
    overflow-y: auto
}

"""}