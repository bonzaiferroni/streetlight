package streetlight.web.pages

import koala.SvgFile
import koala.css.BorderRadius50P
import koala.css.MoonShadow
import koala.css.OpacityHigh
import koala.css.Size100P
import koala.css.modify
import koala.html.Id
import koala.html.image
import koala.html.setId
import kotlinx.html.FlowOrInteractiveOrPhrasingContent

fun FlowOrInteractiveOrPhrasingContent.starBadge() {
    image(SvgFile.Someone, modify(OpacityHigh, Size100P, BorderRadius50P, MoonShadow)) {
        setId(StarBadgeKey.Id)
    }
}

object StarBadgeKey {
    val Id = Id("star-badge")
}