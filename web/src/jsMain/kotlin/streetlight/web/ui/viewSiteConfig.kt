package streetlight.web.ui

import koala.modifier.*
import koala.dom.*
import koala.html.Id
import streetlight.web.model.SiteConfig
import streetlight.web.model.ThemeEditor

fun ViewScope.viewSiteConfig() {
    val model = app.get<SiteConfig>()

    configBody("Site", "Config", "viewSiteConfig.kt") {
        lazyTabs(Id("site-config-tabs")) {
//            tab("general") {
//                column(AlignItemsStart) {
//                    // switch("show transit", config.showTransitState)
//
//                }
//            }
            tab("default theme") {
                val editor = ThemeEditor(model.pageThemeState.now)
                val messenger = MessageStore()
                formRow {
                    formSection("Theme Override") {
                        row {
                            switch("use my theme", model.defaultThemeOnlyState)
                            centeredText(
                                "This allows you to override custom themes set by Streetlight content to maintain a consistent visual style.",
                                Flex1,
                            )
                        }
                    }
                    formSection("Day / Night") {
                        dropMenu(model.themeState)
                    }
                }
                themeForm(editor)
                formSubmit("save", {
                    val theme = editor.buildTheme() ?: return@formSubmit
                    model.pageThemeState.set(theme)
                }, messenger)
            }
        }
    }
}