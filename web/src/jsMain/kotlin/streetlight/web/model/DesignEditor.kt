package streetlight.web.model

import kampfire.model.Messenger
import streetlight.model.data.DefaultLayout
import streetlight.model.data.PageDesign
import streetlight.model.data.PageLayout
import streetlight.model.data.PageTheme
import streetlight.web.io.ApiClient

class DesignEditor(
    api: ApiClient,
    defaultLayout: PageLayout,
    initialLayout: PageLayout?,
    initialTheme: PageTheme?
) {
    val layout = LayoutEditor(initialLayout, defaultLayout, api)
    val theme = ThemeEditor(initialTheme)

    suspend fun build(messenger: Messenger): PageDesign? {
        val theme = theme.buildTheme()
        val layout = layout.buildLayout(messenger)
        return if (theme != null || layout != null) PageDesign(layout, theme) else null
    }
}