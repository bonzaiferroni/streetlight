package streetlight.web.ui

import kampfire.api.Slug
import kampfire.model.handleResponse
import koala.css.Secondary
import koala.css.modify
import koala.dom.AppScope
import koala.dom.button
import koala.dom.column
import koala.dom.dangerButton
import koala.html.btn
import kotlinx.coroutines.launch
import streetlight.web.PostUpdateRoute

fun AppScope.postMenu(slug: Slug) {
    column {
        btn("edit", PostUpdateRoute(slug), modify(Secondary))
        button("report", modify(Secondary))
        dangerButton("remove", onClick = {
            parentScope.launch {
                api.removePost(slug).handleResponse(toaster::toast) {
                    portal.refresh()
                }
            }
        })
    }
}