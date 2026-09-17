package streetlight.web.ui

import koala.modifier.*

object LayoutStyle {
    val Image = Class("layout-image")
    val Gallery = Class("layout-gallery")
}

// language="CSS"
val LayoutStyleCss get() = with(LayoutStyle) { """
$Image {
    margin: auto;
    overflow: clip;
}

$Gallery {
    display: grid;
    grid-template-columns: repeat(var(${Property.ColumnCount.identifier}), minmax(0, 1fr));
    gap: var(--unit-1);
}
""" }