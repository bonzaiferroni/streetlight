package streetlight.web.ui

import koala.css.Secondary
import koala.css.modify
import koala.dom.RenderContext
import koala.dom.button
import koala.dom.column
import koala.dom.dangerButton
import koala.html.btn
import kotlinx.coroutines.launch
import streetlight.model.data.PostId
import streetlight.web.EditPostRoute
import streetlight.web.io.ApiClient
import streetlight.web.io.handleResponse
import streetlight.web.model.GalaxyStage
import streetlight.web.model.Toaster

fun RenderContext.postMenu(postId: PostId) {
    val api = app.get<ApiClient>()
    val toaster = app.get<Toaster>()
    val stage = app.get<GalaxyStage>()

    column {
        btn("edit", EditPostRoute(postId), modify(Secondary))
        button("report", modify(Secondary))
        dangerButton("remove", onClick = {
            renderScope.launch {
                api.removePost(postId).handleResponse(toaster::toast) {
                    stage.removePost(postId)
                }
            }
        })
    }
}