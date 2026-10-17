package streetlight.web.view

import koala.SvgFile
import koala.dom.*
import streetlight.web.model.AccountEditor
import koala.dom.layout.*
import streetlight.model.ui.StarConfigRoute
import streetlight.model.ui.StarConfigTab
import streetlight.web.dom.layout.formRow

context(model: AccountEditor)
fun ViewScope.starConfigTabs() = tabs(paramKey = StarConfigRoute.tabParam.key) {
    StarConfigTab.entries.forEach {
        tab(it.label, it.svg) {
            when (it) {
                StarConfigTab.Account -> {
                    formRow {
                        changeEmailForm()
                        changePasswordForm()
                    }
                }
                StarConfigTab.ConnectApps -> {
                    connectAppsPanel()
                }
            }
        }
    }
}
