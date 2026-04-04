package streetlight.web.pages

import koala.SvgFile
import koala.css.Class
import koala.css.OpacityHalf
import koala.css.OpacityMost
import koala.css.Size100P
import koala.css.modify
import koala.html.icon
import koala.html.image
import kotlinx.html.FlowContent
import kotlinx.html.FlowOrInteractiveOrPhrasingContent

fun FlowOrInteractiveOrPhrasingContent.starBadge() {
    image(SvgFile.EmptyProfile, modify(StarBadgeKey.Class, OpacityMost, Size100P))
}

object StarBadgeKey {
    val Class = Class("star-badge")
}