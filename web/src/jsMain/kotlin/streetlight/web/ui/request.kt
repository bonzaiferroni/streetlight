package streetlight.web.ui

import kampfire.model.Outcome
import kampfire.model.handleResponse
import koala.dom.ViewScope
import koala.dom.column
import koala.dom.onView
import koala.dom.mountChildView

fun <T> ViewScope.request(
    requestData: suspend () -> Outcome<T>,
    content: ViewScope.(T) -> Unit
) {
    val element = column()
    element.onView {
        launchEffect {
            requestData().handleResponse(toaster) { data ->
                this@request.mountChildView("request", element) {
                    content(data)
                }
            }
        }
    }
}