package streetlight.web.view

import koala.dom.*
import koala.page.Id
import streetlight.web.model.AccountEditor
import koala.dom.layout.*
import streetlight.web.dom.layout.formRow

context(model: AccountEditor)
fun ViewScope.starConfigTabs() = tabs(Id("star-config-tabs")) {
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
