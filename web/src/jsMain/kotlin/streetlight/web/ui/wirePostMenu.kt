package streetlight.web.ui

import kampfire.api.Username
import kampfire.model.handleResponse
import koala.css.BlurBackdrop
import koala.css.BorderRadius3
import koala.css.BorderSolid2Px
import koala.css.Magic
import koala.css.SlideUp
import koala.css.Zen
import koala.css.modify
import koala.dom.ViewScope
import koala.dom.button
import koala.dom.card
import koala.dom.column
import koala.dom.safetyButton
import koala.dom.getAttribute
import koala.dom.closePopover
import koala.dom.flowBlock
import koala.dom.onClick
import koala.dom.popoverRaw
import koala.dom.popover
import koala.html.Attribute
import koala.model.storeOf
import kotlinx.browser.document
import kotlinx.coroutines.launch
import kotlinx.dom.clear
import kotlinx.html.dom.append
import org.w3c.dom.HTMLElement
import streetlight.model.data.PostId

fun ViewScope.wirePostMenu() {
    popoverMenu(PostMenu.PopoverId, {
        val username = it.getAttribute(Attribute.Username) ?: return@popoverMenu null
        val postId = it.getAttribute(PostMenu.PostId) ?: return@popoverMenu null
        PostValue(postId, username)
    }) { (postId, username) ->
        val isUser = session.stateNow.star?.username == username
        column {
            if (isUser) {
                // td: figure out what edit options to provide here
                // do we edit the post or the target record?
                // btn("edit", MediaUpdateRoute(postId), modify(Secondary))
            } else {
                button("report", mod = modify(Zen))
            }
            safetyButton("remove", onConfirm = {
                launchEffect("remove post") {
                    api.removePost(postId).handleResponse(toaster) {
                        portal.refresh()
                    }
                }
            })
        }
    }
}

private data class PostValue(
    val postId: PostId,
    val username: Username,
)

