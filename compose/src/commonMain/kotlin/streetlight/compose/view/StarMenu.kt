package streetlight.compose.view

import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import koala.SvgPack
import koala.SvgFile
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import streetlight.compose.model.StarConfigRoute
import streetlight.compose.model.StarSession
import streetlight.compose.model.currentPortal
import streetlight.compose.modifier.padding
import streetlight.compose.modifier.pointerClick
import streetlight.compose.utility.collectAsState

/**
 * The star's menu: their name beside their [badge], then the [SignInForm] while no star is signed in, or the star's
 * places and the way to sign out, which closes the menu with [onClose]. The cousin of the web's
 * star helm.
 */
@Composable
fun StarMenu(onClose: () -> Unit, badge: @Composable () -> Unit) {
    val theme = currentTheme
    val session = koinInject<StarSession>()
    val star by session.starState.collectAsState()

    Column(1, Modifier.width(theme.unit * MenuWidthUnits).padding(1), horizontalAlignment = Alignment.End) {
        Row(1, verticalAlignment = Alignment.CenterVertically) {
            BasicText(star?.username?.value ?: "Someone", style = theme.text.heading3, color = { theme.colors.ink })
            badge()
        }
        when (star) {
            null -> SignInForm()
            else -> StarPlaces(session, onClose)
        }
    }
}

/**
 * The signed-in star's places, each closing the menu as it opens, those the app has no route for yet opening nothing,
 * and the way to sign out of [session].
 */
@Composable
private fun StarPlaces(session: StarSession, onClose: () -> Unit) {
    val scope = rememberCoroutineScope()
    val portal = currentPortal

    StarItem("Dashboard", SvgFile.Dashboard)
    StarItem("Account", SvgFile.User) {
        portal.open(StarConfigRoute)
        onClose()
    }
    StarItem("Inbox", SvgFile.Mail)
    StarItem("Sign out", SvgFile.SignOut) {
        scope.launch {
            session.signOut()
            onClose()
        }
    }
}

/** A place in the menu, [label] before the large icon of [svg], opening it with [onClick] when given. */
@Composable
private fun StarItem(label: String, svg: SvgPack, onClick: (() -> Unit)? = null) {
    val theme = currentTheme
    Row(1, onClick?.let { Modifier.pointerClick(it) } ?: Modifier, verticalAlignment = Alignment.CenterVertically) {
        BasicText(label, style = theme.text.regular, color = { theme.colors.ink })
        Icon(svg.large, color = { theme.colors.ink }, size = theme.unit * HelmIconUnits)
    }
}

/** The width of the menu in units, the web's `MinWidth(32)`. */
private const val MenuWidthUnits = 32
