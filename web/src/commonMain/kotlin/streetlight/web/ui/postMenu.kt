package streetlight.web.ui

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
import koala.html.uuidAttributeOf
import kotlinx.html.FlowContent
import streetlight.model.data.PostId

fun FlowContent.postMenu(postId: PostId, username: Username?) {
    // val anchor = PositionAnchor("menu-${slug}")
    button(modify(AlignSelfCenter)) {
        setPopoverTarget(PostMenu.PopoverId)
        setAttribute(PostMenu.PostId.to(postId))
        setAttribute(Attribute.Username.to(username))
        icon(SvgFile.Dots, modify(Height3))
    }
}

object PostMenu {
    val PopoverId = Id("post-menu-popover")
    val PostId = uuidAttributeOf("post-menu-slug") { PostId(it) }
}