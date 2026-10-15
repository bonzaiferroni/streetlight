package streetlight.web.view

import koala.dom.*
import streetlight.web.model.AccountEditor
import koala.dom.layout.*
import streetlight.model.ui.StarConfigRoute
import streetlight.web.dom.layout.formRow

context(model: AccountEditor)
fun ViewScope.starConfigTabs() = tabs(paramKey = StarConfigRoute.tabIndexParam.key) {
    tab("account") {
        formRow {
            changeEmailForm()
            changePasswordForm()
        }
    }
    tab("connect apps") {
        connectAppsPanel()
    }
    tab("e-commerce") {
        sellerConfigPanel()
    }
}
