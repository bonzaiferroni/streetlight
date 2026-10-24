package streetlight.compose.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import streetlight.compose.model.EventFeed
import streetlight.compose.model.Portal
import streetlight.compose.utility.collectAsState

/** The view of [streetlight.compose.model.EventFeedRoute]. */
context(portal: Portal, feed: EventFeed)
@Composable
fun EventFeedView() {
    val events by feed.eventsState.collectAsState()

    Column(Modifier.fillMaxSize()) {
        BasicText("Back", Modifier.clickable { portal.back() })
        if (events.isEmpty()) BasicText("No events yet")
        events.forEach { BasicText(it.title) }
    }
}
