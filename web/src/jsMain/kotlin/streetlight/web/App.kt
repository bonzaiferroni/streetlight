package streetlight.web

import io.kvision.*
import io.kvision.panel.root
import io.kvision.routing.Routing
import io.kvision.theme.Theme
import io.kvision.theme.ThemeManager
import streetlight.web.core.AppContext
import streetlight.web.core.Pages
import streetlight.web.core.buildPortal
import streetlight.web.io.stores.AppModel

class App : Application() {

    init {
        require("./css/kvapp.css")
        require("./css/tw.css")
        require("./css/forms.css")
        require("./css/buttons.css")
        ThemeManager.init(initialTheme = Theme.DARK, remember = false)
    }

    override fun start() {
        val routing = Routing.init()
        val context = AppContext(AppModel(), routing)
        root("kvapp") {
            buildPortal(context)
                .addPages(Pages.basePages)
                .addPages(Pages.userPages)
                .build()
        }
    }
}

fun main() {
    startApplication(
        ::App, module.hot,
        BootstrapModule,
        // BootstrapCssModule,
        CoreModule,
        FontAwesomeModule
    )
}