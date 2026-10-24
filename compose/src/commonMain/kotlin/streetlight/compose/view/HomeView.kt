package streetlight.compose.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import streetlight.compose.model.EventFeedRoute
import streetlight.compose.model.Portal

/** The view of [streetlight.compose.model.HomeRoute]. */
context(portal: Portal)
@Composable
fun HomeView() {
    val colors = LocalAppColors.current

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        BasicText("Ahoy, Streetlight", color = { colors.ink })
        BasicText("Events", Modifier.clickable { portal.open(EventFeedRoute) }, color = { colors.accentFg })
    }
}
