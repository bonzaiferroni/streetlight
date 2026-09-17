package streetlight.web.ui

import kampfire.api.Username
import koala.SvgFile
import koala.modifier.*
import koala.html.Id
import koala.html.button
import koala.html.icon
import koala.modifier.setAttribute
import koala.modifier.setPopoverTarget
import koala.modifier.idAttributeOf
import kotlinx.html.FlowContent
import streetlight.model.data.PostId

fun FlowContent.postMenu(postId: PostId, username: Username?) {
    // val anchor = PositionAnchor("menu-${slug}")
    button(modify(AlignSelfCenter)) {
        setPopoverTarget(PostMenu.PopoverId)
        setAttribute(PostMenu.PostId.to(postId))
        setAttribute(Attribute.Username.to(username))
        icon(SvgFile.Dots, modify(SmallIconHeight))
    }
}

object PostMenu {
    val PopoverId = Id("post-menu-popover")
    val PostId = idAttributeOf("post-menu-slug") { PostId(it) }
}