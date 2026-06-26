package streetlight.web.ui

import kampfire.model.Response
import kampfire.model.handleResponse
import koala.dom.AppScope
import koala.dom.RenderScope
import koala.dom.column
import koala.dom.onView
import kotlinx.html.dom.append

fun <T> AppScope.request(
    requestData: suspend () -> Response<T>?,
    content: AppScope.(T) -> Unit
) {
    val element = column()
    element.onView {
        launchEffect {
            requestData().handleResponse(toaster::toast) { data ->
                element.append {
                    val scope = RenderScope(this, app, parentScope, element)
                    scope.content(data)
                }
            }
        }
    }
}