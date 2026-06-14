package streetlight.web.ui

import kampfire.api.Slug
import koala.SvgFile
import koala.css.Height3
import koala.css.PositionAnchor
import koala.css.modify
import koala.css.setAnchorName
import koala.html.Id
import koala.html.button
import koala.html.icon
import koala.html.setAttribute
import koala.html.setPopoverTarget
import koala.html.slugAttributeOf
import koala.html.uuidAttributeOf
import kotlinx.html.FlowContent
import streetlight.model.data.PostId

fun FlowContent.postMenu(slug: Slug) {
    // val anchor = PositionAnchor("menu-${slug}")
    button {
        setPopoverTarget(PostMenu.MenuId)
        setAttribute(PostMenu.Attribute.to(slug))
        icon(SvgFile.Dots, modify(Height3))
    }
}

object PostMenu {
    val MenuId = Id("post-menu")
    val Attribute = slugAttributeOf("post-menu-slug")

}