package streetlight.web.ui

import kampfire.api.Username
import kampfire.model.toDataOr
import koala.modifier.*
import koala.dom.*
import koala.modifier.Attribute
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
                    api.removePost(postId).toDataOr(toaster) { return@launchEffect }
                    portal.refresh()
                }
            })
        }
    }
}

private data class PostValue(
    val postId: PostId,
    val username: Username,
)

