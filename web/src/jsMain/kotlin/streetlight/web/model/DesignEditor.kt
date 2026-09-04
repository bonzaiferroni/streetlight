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
    initialDesign: PageDesign?
) {
    val layout = LayoutEditor(initialDesign?.layout, defaultLayout, api)
    val theme = ThemeEditor(initialDesign?.theme)

    suspend fun build(messenger: Messenger): PageDesign? {
        val theme = theme.buildTheme()
        val layout = layout.buildLayout(messenger)
        return if (theme != null || layout != null) PageDesign(layout, theme) else null
    }
}