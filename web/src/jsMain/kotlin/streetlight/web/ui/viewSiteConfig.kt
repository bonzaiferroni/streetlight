package streetlight.web.ui

import koala.modifier.AlignItemsStart
import koala.modifier.modify
import koala.dom.ViewScope
import koala.dom.column
import koala.dom.dropMenu
import koala.dom.switch
import streetlight.web.model.SiteConfig

fun ViewScope.viewSiteConfig() {
    val config = app.get<SiteConfig>()

    configBody("Site", "Config", "viewSiteConfig.kt") {
        column(AlignItemsStart) {
            switch("show transit", config.showTransitState)
            dropMenu(config.themeFlow)
        }
    }
}