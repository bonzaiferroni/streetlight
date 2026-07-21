package streetlight.web.ui

import kampfire.model.Outcome
import kampfire.model.handleResponse
import koala.css.ModifierSet
import koala.dom.ViewScope
import koala.dom.column
import koala.dom.onView
import koala.dom.mountChildView

fun <T> ViewScope.dataBlock(
    requestData: suspend () -> Outcome<T>,
    mod: ModifierSet? = null,
    content: ViewScope.(T) -> Unit
) {
    val element = column(mod)
    element.onView {
        launchEffect {
            requestData().handleResponse(toaster) { data ->
                this@dataBlock.mountChildView("request", element) {
                    content(data)
                }
            }
        }
    }
}