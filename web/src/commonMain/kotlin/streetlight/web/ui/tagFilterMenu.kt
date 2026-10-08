package streetlight.web.ui

import koala.SvgFile
import koala.html.Id
import koala.html.button
import koala.html.icon
import koala.modifier.*
import kotlinx.html.FlowContent

/** Opens the menu that filters a feed by tag. */
fun FlowContent.tagFilterMenu() {
    button(AlignSelfCenter) {
        setPopoverTarget(TagFilterMenu.PopoverId)
        icon(SvgFile.Label, SmallIconHeight)
    }
}

object TagFilterMenu {
    val PopoverId = Id("tag-filter-popover")
}
