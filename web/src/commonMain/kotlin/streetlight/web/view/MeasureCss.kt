package streetlight.web.view

import koala.modifier.*

object MeasureStyle {
    val Strip = Class("measure-strip")
    val Measure = Strip.withBemElement("measure")
}

//language="CSS"
val MeasureCss get() = with(MeasureStyle) { """

$Strip {
    display: flex;
    border-inline-end: 1px solid currentColor;
    opacity: 0.5;
}

$Measure { border-inline-start: 1px solid currentColor; }
"""}
