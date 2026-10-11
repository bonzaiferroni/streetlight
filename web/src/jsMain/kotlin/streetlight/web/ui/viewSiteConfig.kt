package streetlight.web.ui

import koala.modifier.*
import koala.dom.*
import streetlight.web.model.SiteConfig
import streetlight.web.model.ThemeEditor

fun ViewScope.viewSiteConfig() {
    val model = app.get<SiteConfig>()

    configBody("Site", "Config", "viewSiteConfig.kt") {
        lazyTabs {
            tab("general") {
                column(AlignItemsStart) {
                    // switch("show transit", config.showTransitState)
                    switch("default theme only", model.defaultThemeOnlyState)
                    dropMenu(model.themeFlow)
                }
            }
            tab("default theme") {
                val editor = ThemeEditor(model.themeState.now)
                val messenger = MessageStore()
                themeForm(editor)
                formSubmit("save", {
                    val theme = editor.buildTheme() ?: return@formSubmit
                    model.themeState.set(theme)
                }, messenger)
            }
        }
    }
}