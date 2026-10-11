package streetlight.web.pages

import koala.SvgFile
import koala.modifier.BorderRadius50P
import koala.modifier.MoonShadow
import koala.modifier.OpacityHigh
import koala.modifier.Size100P
import koala.modifier.modify
import koala.page.Id
import koala.component.image
import koala.page.setId
import kotlinx.html.FlowOrInteractiveOrPhrasingContent

/** The signed-in star's image, filled in by the client. */
fun FlowOrInteractiveOrPhrasingContent.starBadge() {
    image(SvgFile.Someone, modify(OpacityHigh, Size100P, BorderRadius50P, MoonShadow)) {
        setId(StarBadgeKey.Id)
    }
}

object StarBadgeKey {
    val Id = Id("star-badge")
}