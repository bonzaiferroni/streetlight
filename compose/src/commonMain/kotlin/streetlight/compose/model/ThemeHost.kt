package streetlight.compose.model

import kampfire.model.Tap
import kampfire.model.storeOf
import streetlight.model.data.PageTheme

/** The theme the app shows, set by the view whose content carries one, or `null` for the viewer's default theme. */
class ThemeHost {
    private val store = storeOf<PageTheme?>(null)

    val themeState: Tap<PageTheme?> = store

    fun setTheme(theme: PageTheme?) = store.set(theme)
}
