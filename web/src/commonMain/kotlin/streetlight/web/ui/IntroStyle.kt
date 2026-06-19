package streetlight.web.ui

import koala.css.*
import kotlinx.css.GridTemplateColumns
import kotlinx.css.fr
import kotlinx.css.rem

object IntroStyle {
    val Columns = GridTemplateColumns(1.fr, 12.rem)
    val SectionMod = modify(PaddingLeft1, PaddingTop1)
}