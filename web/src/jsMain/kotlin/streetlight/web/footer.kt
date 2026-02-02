package streetlight.web

import koala.css.AlignItemsCenter
import koala.css.Gap0
import koala.css.Italic
import koala.css.Opacity6
import koala.css.Width100
import koala.css.modify
import koala.dom.RenderContext
import koala.dom.lottie
import koala.dom.row
import koala.html.column
import koala.html.paragraph
import koala.html.row
import kotlinx.html.style

fun RenderContext.footer() {
    val giants = "May we choose a world of good and faithful giants. "
    row(modify(AlignItemsCenter)) {
        style = "height: 20rem;"
        column(modify(AlignItemsCenter, Gap0, Width100)) {
            lottie("spinning_circles") {
                style = "height: 10rem;"
            }
            paragraph(giants, modify(Italic, Opacity6))
        }
    }
}