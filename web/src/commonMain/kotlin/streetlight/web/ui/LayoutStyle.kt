package streetlight.web.ui

import koala.css.*

object LayoutStyle {
    val Image = Class("layout-image")
}

// language="CSS"
val LayoutStyleCss get() = with(LayoutStyle) { """
$Image {
    margin: auto;
    overflow: clip;
}
""" }