package streetlight.web.view

import koala.SvgFile
import koala.dom.*
import streetlight.web.model.AccountEditor
import koala.dom.layout.*
import streetlight.model.ui.StarConfigRoute
import streetlight.web.dom.layout.formRow

context(model: AccountEditor)
fun ViewScope.starConfigTabs() = tabs(paramKey = StarConfigRoute.tabIndexParam.key) {
    tab("account", SvgFile.User) {
        formRow {
            changeEmailForm()
            changePasswordForm()
        }
    }
    tab("connect apps", SvgFile.Affiliate) {
        connectAppsPanel()
    }
    tab("e-commerce", SvgFile.BuildingStore) {
        sellerConfigPanel()
    }
}
