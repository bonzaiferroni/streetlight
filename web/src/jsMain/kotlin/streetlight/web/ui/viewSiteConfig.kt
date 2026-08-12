package streetlight.web.ui

import koala.css.AlignItemsStart
import koala.css.modify
import koala.dom.ViewScope
import koala.dom.column
import koala.dom.dropMenu
import koala.dom.switch
import koala.html.topLogo
import streetlight.web.model.SiteConfig

fun ViewScope.viewSiteConfig() {
    val config = app.get<SiteConfig>()

    column(modify(AlignItemsStart)) {
        topLogo()
        switch("show transit", config.showTransitState)
        dropMenu(config.themeFlow)
    }
}