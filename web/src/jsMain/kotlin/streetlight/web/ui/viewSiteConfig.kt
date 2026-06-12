package streetlight.web.ui

import koala.css.AlignItemsStart
import koala.css.modify
import koala.dom.AppScope
import koala.dom.column
import koala.dom.dropMenu
import koala.dom.switch
import streetlight.web.model.SiteConfig

fun AppScope.viewSiteConfig() {
    val config = app.get<SiteConfig>()

    column(modify(AlignItemsStart)) {
        switch("show transit", onToggle = config::setShowTransit, bindFlow = config.showTransitFlow)
        dropMenu(config::setTheme, { it.name }, config.themeFlow)
    }
}