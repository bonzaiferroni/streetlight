package streetlight.web.view

import koala.modifier.*
import koala.dom.*
import koala.page.Id
import streetlight.web.model.SiteConfig
import streetlight.web.model.ThemeEditor
import koala.dom.component.*
import koala.dom.layout.*
import koala.model.*
import streetlight.web.dom.layout.centeredText
import streetlight.web.dom.layout.configBody
import streetlight.web.dom.layout.formRow
import streetlight.web.dom.layout.formSection
import streetlight.web.dom.layout.formSubmit

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
                        row(AlignItemsCenter) {
                            switch("override", model.defaultThemeOnlyState)
                            centeredText("Override custom themes to maintain a consistent visual style.", Flex1,)
                        }
                    }
                    formSection("Day / Night") {
                        optionSwitch(model.themeState)
                    }
                }
                context(editor) {
                    themeForm()
                }
                formSubmit("save", {
                    val theme = editor.buildTheme() ?: return@formSubmit
                    model.pageThemeState.set(theme)
                }, messenger)
            }
        }
    }
}