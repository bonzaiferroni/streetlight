package streetlight.web.view

import koala.dom.RouteScope
import koala.dom.component.textBlock
import streetlight.model.ui.Screen
import streetlight.web.model.loadStudioModule

/** The view of the current screen in the Studio section, loading the section's services on first use. */
fun RouteScope.viewStudioPortal() {
    app.loadStudioModule()
    when (val screen = state.route.screen) {
        Screen.Studio -> viewStudioRoute()
        Screen.MusicStudio -> viewMusicStudioRoute()
        else -> textBlock("Coming soon: $screen")
    }
}
