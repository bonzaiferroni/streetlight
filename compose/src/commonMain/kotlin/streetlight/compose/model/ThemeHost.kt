package streetlight.compose.model

import kampfire.model.Tap
import kampfire.model.storeOf
import streetlight.model.data.PageTheme

/** The theme the app shows, set by the view whose content carries one. */
class ThemeHost {
    private val store = storeOf(PageTheme())

    val themeState: Tap<PageTheme> = store

    /** Shows [theme], or the default theme when it is `null`. */
    fun setTheme(theme: PageTheme?) {
        store.set(theme ?: PageTheme())
    }
}
