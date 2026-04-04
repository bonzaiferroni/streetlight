package streetlight.web.pages

import koala.SvgFile
import koala.css.BorderRadius50P
import koala.css.Class
import koala.css.OpacityMost
import koala.css.Size100P
import koala.css.modify
import koala.html.image
import kotlinx.html.FlowOrInteractiveOrPhrasingContent

fun FlowOrInteractiveOrPhrasingContent.starBadge() {
    image(SvgFile.Someone, modify(StarBadgeKey.Class, OpacityMost, Size100P, BorderRadius50P))
}

object StarBadgeKey {
    val Class = Class("star-badge")
}