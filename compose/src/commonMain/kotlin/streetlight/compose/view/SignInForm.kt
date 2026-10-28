package streetlight.compose.view

import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kampfire.model.MutableTap
import kampfire.model.Ok
import kampfire.model.storeOf
import koala.SvgFile
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import streetlight.compose.model.StarSession
import streetlight.compose.model.UserCreator
import streetlight.compose.utility.collectAsState
import streetlight.model.io.ApiClient

/**
 * The fields of a username or email and a password, a problem from the last try, and the buttons to create an account,
 * which opens its dialog, and to sign in, held to the end of its column. The cousin of the web's `signInForm`.
 */
@Composable
fun SignInForm(modifier: Modifier = Modifier) {
    val theme = currentTheme
    val session = koinInject<StarSession>()
    val scope = rememberCoroutineScope()
    val identity = remember { storeOf("") }
    val password = remember { storeOf("") }
    var problem by remember { mutableStateOf<String?>(null) }
    var isSigningIn by remember { mutableStateOf(false) }
    val isCreating = remember { storeOf(false) }

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

    Column(1, modifier, horizontalAlignment = Alignment.End) {
        TextField(identity, "username or email", icon = SvgFile.User)
        TextField(password, "password", icon = SvgFile.Key, isPassword = true, onSubmit = signIn)
        problem?.let { BasicText(it, style = theme.text.small, color = { theme.colors.red }) }
        Row(1) {
            Button("create account", onClick = { isCreating.set(true) }, style = ButtonStyle.Zen)
            Button("sign in", onClick = signIn, isEnabled = !isSigningIn)
        }
    }
    CreateAccountDialog(isCreating, session)
}

/**
 * The dialog to create an account while [state] is `true`: a tab for each kind of account, a guest's or a registered
 * star's, each form on its card, sharing one [UserCreator]. It closes once the new star is signed in. The cousin of the
 * web's sign-in dialog, whose third tab, signing in, is the [SignInForm].
 */
@Composable
private fun CreateAccountDialog(state: MutableTap<Boolean>, session: StarSession) {
    RawDialogMount(state, "Create Account") {
        val api = koinInject<ApiClient>()
        val scope = rememberCoroutineScope()
        val creator = remember { UserCreator(scope, api, session) }
        val star by session.starState.collectAsState()
        LaunchedEffect(star) { if (star != null) state.set(false) }

        context(creator) {
            Tabs {
                tab("Guest", SvgFile.Ghost) { DialogCard { GuestRegistrationForm() } }
                tab("Register", SvgFile.UserPlus) { DialogCard { FullRegistrationForm() } }
            }
        }
    }
}
