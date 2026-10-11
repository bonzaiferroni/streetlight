package streetlight.web.component

import kampfire.api.Username
import koala.SvgFile
import koala.modifier.*
import koala.page.Id
import koala.component.button
import koala.component.icon
import koala.modifier.setAttribute
import koala.modifier.setPopoverTarget
import koala.modifier.idAttributeOf
import kotlinx.html.FlowContent
import streetlight.model.data.PostId
import streetlight.web.page.PopoverId

/** Opens the menu of the post [postId]. */
fun FlowContent.postMenu(postId: PostId, username: Username?) {
    // val anchor = PositionAnchor("menu-${slug}")
    button(AlignSelfCenter) {
        setPopoverTarget(PostMenu.PopoverId)
        setAttribute(PostMenu.PostId.to(postId))
        setAttribute(Attribute.Username.to(username))
        icon(SvgFile.Dots.small, SmallIconHeight)
    }
}

object PostMenu {
    val PopoverId = Id("post-menu-popover")
    val PostId = idAttributeOf("post-menu-slug") { PostId(it) }
}