package streetlight.compose.view

import streetlight.model.ui.TopBar
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import koala.model.MessageStore
import org.koin.compose.koinInject
import streetlight.compose.model.AppConfig
import streetlight.compose.model.ThemeEditor

/** The screen of `AppConfigRoute`: its name as its top bar, over the [AppConfigView]. */
@Composable
fun AppConfigScreen() {
    val scrollState = rememberScrollState()
    TopBarMount(TopBar.TwoTone("App", "Config"), scrollState)
    context(koinInject<AppConfig>()) { AppConfigView(scrollState) }
}

/**
 * The view of `AppConfigRoute`, in a column scrolled by [scrollState] under the top bar: the viewer's settings, in
 * tabs. The cousin of the web's `viewSiteConfig`.
 */
@Composable
context(config: AppConfig)
fun AppConfigView(scrollState: ScrollState) {
    ConfigBody(null, scrollState) {
        Tabs {
            tab("default theme") { DefaultThemeTab() }
        }
    }
}

/**
 * Whether every page keeps the default theme, the day or night theme, and the default theme's form, saved with a
 * message beside the button.
 */
@Composable
context(config: AppConfig)
private fun DefaultThemeTab() {
    val editor = remember { ThemeEditor(config.defaultThemeState.now) }
    val messages = remember { MessageStore() }

    Column(2) {
        ColumnRow {
            FormSection("Theme Override") {
                Row(1, verticalAlignment = Alignment.CenterVertically) {
                    Switch(config.isDefaultThemeOnlyState, "override")
                    CenteredText("Override custom themes to maintain a consistent visual style.")
                }
            }
            FormSection("Day / Night") { Placeholder("The day and night themes") }
        }
        context(editor) { ThemeForm() }
        FormSubmit("save", {
            config.defaultThemeState.set(editor.themeState.now)
            messages.deliverSuccess("Saved the default theme.")
        }, messages)
    }
}
