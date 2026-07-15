package streetlight.web.ui

import kampfire.model.Outcome
import kampfire.model.handleResponse
import koala.dom.AppScope
import koala.dom.column
import koala.dom.onView
import koala.dom.replaceStaticRender

fun <T> AppScope.request(
    requestData: suspend () -> Outcome<T>?,
    content: AppScope.(T) -> Unit
) {
    val element = column()
    element.onView {
        launchEffect {
            requestData().handleResponse(toaster) { data ->
                element.replaceStaticRender(app, parentScope) {
                    content(data)
                }
            }
        }
    }
}