package streetlight.web.ui

import koala.css.*
import koala.markdown.MarkdownStyle.Container

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
    grid-template-columns: repeat(var(${Property.ColumnCount.expression}), minmax(0, 1fr));
    gap: var(--unit-spacing-1);
}
""" }