package streetlight.compose.view

import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kampfire.model.Ok
import kampfire.model.storeOf
import koala.SvgPack
import koala.SvgFile
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import streetlight.compose.model.StarSession
import streetlight.compose.model.currentPortal
import streetlight.compose.modifier.padding
import streetlight.compose.modifier.pointerClick
import streetlight.compose.utility.collectAsState

/**
 * The star's menu: their name beside their [badge], then the form to sign in while no star is signed in, or the
 * star's places and the way to sign out, which closes the menu with [onClose] and returns home. The cousin of the web's
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
            null -> SignInForm(session)
            else -> StarPlaces(session, onClose)
        }
    }
}

/**
 * The fields of a username or email and a password, a problem from the last try, and the buttons to create an account,
 * which does nothing until the app has the dialog, and to sign in through [session].
 */
@Composable
private fun SignInForm(session: StarSession) {
    val theme = currentTheme
    val scope = rememberCoroutineScope()
    val identity = remember { storeOf("") }
    val password = remember { storeOf("") }
    var problem by remember { mutableStateOf<String?>(null) }
    var isSigningIn by remember { mutableStateOf(false) }

    val signIn = signIn@{
        if (isSigningIn || identity.now.isBlank() || password.now.isBlank()) return@signIn
        isSigningIn = true
        scope.launch {
            when (val outcome = session.signIn(identity.now.trim(), password.now)) {
                is Ok -> password.set("")
                else -> problem = outcome.message
            }
            isSigningIn = false
        }
        Unit
    }

    TextField(identity, "username or email", icon = SvgFile.User)
    TextField(password, "password", icon = SvgFile.Key, isPassword = true, onSubmit = signIn)
    problem?.let { BasicText(it, style = theme.text.small, color = { theme.colors.red }) }
    Row(1) {
        Button("create account", onClick = {}, style = ButtonStyle.Zen)
        Button("sign in", onClick = signIn, isEnabled = !isSigningIn)
    }
}

/** The signed-in star's places, which open nothing until the app has them, and the way to sign out of [session]. */
@Composable
private fun StarPlaces(session: StarSession, onClose: () -> Unit) {
    val scope = rememberCoroutineScope()
    val portal = currentPortal

    StarItem("Dashboard", SvgFile.Dashboard)
    StarItem("Account", SvgFile.User)
    StarItem("Inbox", SvgFile.Mail)
    StarItem("Sign out", SvgFile.SignOut) {
        scope.launch {
            session.signOut()
            portal.home()
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
