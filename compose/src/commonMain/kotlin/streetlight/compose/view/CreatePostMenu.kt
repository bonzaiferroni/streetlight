package streetlight.compose.view

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kampfire.api.Slug
import streetlight.compose.model.AppRoute
import streetlight.compose.model.EventScoutRoute
import streetlight.compose.model.LocationScoutRoute
import streetlight.compose.model.MediaForgeRoute
import streetlight.compose.model.currentPortal
import streetlight.compose.modifier.padding

/**
 * The button to create a post and the menu it opens, of the kinds of post, each opening its screen to post to the
 * galaxy of [slug], when given. The cousin of the web's `createPostMenu` and its popover.
 */
@Composable
fun CreatePostMenu(slug: Slug?, modifier: Modifier = Modifier) {
    val portal = currentPortal
    var isOpen by remember { mutableStateOf(false) }
    val openRoute = { route: AppRoute ->
        isOpen = false
        portal.open(route)
    }

    Box(modifier) {
        Button("Create Post", onClick = { isOpen = true }, style = ButtonStyle.Zen)
        Popover(isOpen, onDismiss = { isOpen = false }) {
            Column(1, Modifier.padding(1)) {
                Button("Post Event", onClick = { openRoute(EventScoutRoute(slug)) }, style = ButtonStyle.Zen)
                Button("Post Location", onClick = { openRoute(LocationScoutRoute(slug)) }, style = ButtonStyle.Zen)
                Button("Post Media", onClick = { openRoute(MediaForgeRoute(slug)) }, style = ButtonStyle.Zen)
            }
        }
    }
}
