package streetlight.web.ui

import koala.css.AlignItemsStart
import koala.css.modify
import koala.dom.ViewContext
import koala.dom.column
import koala.dom.dropMenu
import koala.dom.row
import koala.dom.switch
import streetlight.web.model.Streetlight

fun ViewContext<Streetlight>.viewSiteConfig() {
    val app = model
    val config = app.config

    column(modify(AlignItemsStart)) {
        switch("show transit", onToggle = config::setShowTransit, bindFlow = config.showTransitFlow)
        dropMenu(config::setTheme, { it.name }, config.themeFlow)
    }
}