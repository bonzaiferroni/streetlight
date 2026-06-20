package streetlight.web.ui

import kampfire.model.ApiResponse
import kampfire.model.handleResponse
import koala.dom.AppScope
import koala.dom.TagScope
import koala.dom.column
import koala.dom.replaceRender

fun <T> AppScope.request(
    requestData: suspend () -> ApiResponse<T>?,
    content: AppScope.(T) -> Unit
) {
    val element = column()

    launchEffect {
        requestData().handleResponse(toaster::toast) { data ->
            element.replaceRender(app, parentScope) {
                content(data)
            }
        }
    }
}