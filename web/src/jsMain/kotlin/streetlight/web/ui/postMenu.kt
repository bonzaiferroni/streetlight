package streetlight.web.ui

import kampfire.model.Ok
import kampfire.model.Problem
import koala.css.Secondary
import koala.css.modify
import koala.dom.button
import koala.dom.column
import koala.dom.dangerButton
import kotlinx.coroutines.launch
import streetlight.model.data.PostId
import streetlight.web.io.handleResponse

fun AppContext.postMenu(postId: PostId) {
    column {
        button("report", modify(Secondary))
        dangerButton("remove", onClick = {
            renderScope.launch {
                api.removePost(postId).handleResponse(model.toaster::toast) {
                    model.stage.galaxy.removePost(postId)
                }
            }
        })
    }
}