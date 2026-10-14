package streetlight.web.view

import koala.modifier.*
import koala.dom.*
import koala.page.Id
import kotlinx.html.ol
import streetlight.web.model.AccountEditor
import streetlight.web.model.SellerEditor
import streetlight.web.model.api
import streetlight.web.model.toaster
import koala.dom.component.*
import koala.dom.layout.*
import koala.model.*
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
        val model = SellerEditor(contentScope, api.seller, toaster)
        context(model) {
            formRow {
                sellerConfigPanel()
            }
        }
    }
}
