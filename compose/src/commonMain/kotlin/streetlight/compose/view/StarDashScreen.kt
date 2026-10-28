package streetlight.compose.view

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import streetlight.compose.model.StarDashRoute
import streetlight.compose.model.StarSession
import streetlight.compose.model.currentPortal
import streetlight.compose.modifier.Outline
import streetlight.compose.modifier.borderRadius
import streetlight.compose.modifier.cardBg
import streetlight.compose.modifier.padding
import streetlight.model.data.Star
import streetlight.model.ui.StarDashTab

/**
 * The screen of [route]: the logo as its top bar, and, while a star is signed in, the star's dash as its top bar over
 * the [StarDashView].
 */
@Composable
fun StarDashScreen(route: StarDashRoute) {
    val scrollState = rememberScrollState()
    TopBarMount(TopBar.Logo, scrollState)
    StarGate {
        TopBarMount(TopBar.TwoTone("${contextOf<Star>().username.value}'s ", "Dash"), scrollState)
        StarDashView(route, scrollState)
    }
}

/**
 * The view of [route], in a column scrolled by [scrollState] under the top bar that names it: a tab for each
 * [StarDashTab], opening on the route's tab, or the first. The cousin of the web's `viewStarDash`.
 */
@Composable
context(star: Star)
fun StarDashView(route: StarDashRoute, scrollState: ScrollState) {
    ConfigBody(null, scrollState) {
        Tabs(initialTab = route.tab?.ordinal ?: 0) {
            StarDashTab.entries.forEach { dashTab ->
                tab(dashTab.label, dashTab.svg) {
                    when (dashTab) {
                        StarDashTab.Overview -> Placeholder("The star's locations and galaxies")
                        StarDashTab.ECommerce -> Placeholder("The star's store and offerings")
                        StarDashTab.Skills -> Placeholder("The star's skills")
                        StarDashTab.Sandbox -> SandboxTab()
                    }
                }
            }
        }
    }
}

/** A greeting to the star and the ways out, a place to try things. The cousin of the web's `sandboxContent`. */
@Composable
context(star: Star)
private fun SandboxTab() {
    val theme = currentTheme
    val portal = currentPortal
    val session = koinInject<StarSession>()
    val scope = rememberCoroutineScope()

    Column(1) {
        Row(1, Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            BasicText(
                "Hello ${star.username.value}!",
                Modifier.weight(1f),
                style = theme.text.regular,
                color = { theme.colors.ink },
            )
            Button("go home", onClick = portal::home, style = ButtonStyle.Zen)
            Button("sign out", onClick = { scope.launch { session.signOut() } }, style = ButtonStyle.Zen)
        }
    }
}

/** A tab's [subject], which waits for its own work, on a card. */
@Composable
private fun Placeholder(subject: String) {
    val theme = currentTheme
    BasicText(
        "$subject, coming soon",
        Modifier.fillMaxWidth().borderRadius(2, outline = Outline.Mid).cardBg().padding(2),
        style = theme.text.regular.copy(textAlign = TextAlign.Center),
        color = { theme.colors.ink },
    )
}
