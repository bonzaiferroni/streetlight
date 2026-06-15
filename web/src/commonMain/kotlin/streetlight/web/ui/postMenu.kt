package streetlight.web.ui

import kampfire.api.Slug
import kampfire.api.Username
import kampfire.api.toUsername
import koala.SvgFile
import koala.css.AlignSelfCenter
import koala.css.Height3
import koala.css.modify
import koala.html.Attribute
import koala.html.Id
import koala.html.button
import koala.html.icon
import koala.html.setAttribute
import koala.html.setPopoverTarget
import koala.html.slugAttributeOf
import kotlinx.html.FlowContent

fun FlowContent.postMenu(slug: Slug, username: Username?) {
    // val anchor = PositionAnchor("menu-${slug}")
    button(modify(AlignSelfCenter)) {
        setPopoverTarget(PostMenu.MenuId)
        setAttribute(PostMenu.Slug.to(slug))
        setAttribute(PostMenu.Username.to(username))
        icon(SvgFile.Dots, modify(Height3))
    }
}

object PostMenu {
    val MenuId = Id("post-menu")
    val Slug = slugAttributeOf("post-menu-slug")
    val Username = Attribute<Username?>("post-menu-username", true) { it.toUsername() }
}