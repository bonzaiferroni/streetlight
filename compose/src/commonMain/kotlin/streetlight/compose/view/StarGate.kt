package streetlight.compose.view

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import org.koin.compose.koinInject
import streetlight.compose.model.StarSession
import streetlight.compose.modifier.Outline
import streetlight.compose.modifier.borderRadius
import streetlight.compose.modifier.cardBg
import streetlight.compose.modifier.filigree
import streetlight.compose.modifier.padding
import streetlight.compose.utility.collectAsState
import streetlight.model.data.Star

/**
 * Shows [content] with the signed-in star in context, or [fallback] while no star is signed in, switching as the star
 * signs in or out. A screen of the star's own puts it ahead of reading its content, so nothing is read signed out. The
 * cousin of the web's `starGate`.
 */
@Composable
fun StarGate(fallback: @Composable () -> Unit = { SignInToContinue() }, content: @Composable context(Star) () -> Unit) {
    val star by koinInject<StarSession>().starState.collectAsState()
    when (val signedIn = star) {
        null -> fallback()
        else -> context(signedIn) { content() }
    }
}

/** The [SignInForm] on a card under the top bar, asking the star to sign in to see the page. */
@Composable
fun SignInToContinue() {
    val theme = currentTheme
    Box(Modifier.fillMaxSize().padding(currentPageInsets).padding(2), contentAlignment = Alignment.TopCenter) {
        Column(
            1,
            // as wide as its widest row, so the buttons keep their labels, and no narrower than the star menu
            Modifier.width(IntrinsicSize.Max)
                .widthIn(min = theme.unit * CardWidthUnits)
                // its corners follow the curve of the buttons at its bottom
                .borderRadius(3, moonShadow = true, outline = Outline.Mid)
                .cardBg()
                .padding(2),
        ) {
            BasicText(
                "Sign in to continue",
                Modifier.fillMaxWidth().filigree(),
                style = theme.text.heading4.copy(textAlign = TextAlign.Center),
                color = { theme.colors.ink },
            )
            SignInForm(Modifier.fillMaxWidth())
        }
    }
}

/** The narrowest the card runs in units, as the star menu. */
private const val CardWidthUnits = 32
