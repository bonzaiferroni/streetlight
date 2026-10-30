package streetlight.web.component

import koala.SvgFile
import koala.component.IconStyle
import koala.modifier.AccentFg
import koala.modifier.BorderRadius50P
import koala.modifier.Css
import koala.modifier.DisplayNone
import koala.modifier.MoonShadow
import koala.modifier.OpacityHigh
import koala.modifier.Size100P
import koala.modifier.addModifiers
import koala.modifier.modify
import koala.modifier.setStyle
import koala.page.Id
import koala.component.image
import koala.page.setId
import kotlinx.html.FlowOrPhrasingContent
import kotlinx.html.span

/** The star's badge: an icon in the accent while signed out, and the signed-in star's image, shown by the client. */
fun FlowOrPhrasingContent.starBadge() {
    span {
        setId(StarBadgeKey.IconId)
        addModifiers(modify(IconStyle.Icon, Size100P, AccentFg))
        setStyle(Css.MaskUrl.of(SvgFile.Someone))
    }
    image(SvgFile.Someone, modify(OpacityHigh, Size100P, BorderRadius50P, MoonShadow, DisplayNone)) {
        setId(StarBadgeKey.Id)
    }
}

object StarBadgeKey {
    val Id = Id("star-badge")
    val IconId = Id("star-badge-icon")
}
