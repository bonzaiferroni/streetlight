package streetlight.compose.view

import streetlight.model.ui.TopBar
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import streetlight.compose.model.HomeRoute
import streetlight.compose.model.LocationScout
import streetlight.compose.model.LocationScoutFocus
import streetlight.compose.model.LocationScoutRoute
import streetlight.compose.model.LocationScoutStage
import streetlight.compose.model.PostGalaxy
import streetlight.compose.model.currentPortal
import streetlight.compose.model.toRoute
import streetlight.compose.utility.collectAsState
import streetlight.model.data.FeedContext
import streetlight.model.data.Location
import streetlight.model.data.LocationEdit
import streetlight.model.data.Star

/**
 * The screen of [route]: its name as its top bar, and, while a star is signed in, the galaxy the location goes to once
 * read, if any, scouted in the [LocationScoutView].
 */
@Composable
fun LocationScoutScreen(route: LocationScoutRoute) {
    val scrollState = rememberScrollState()
    TopBarMount(TopBar.TwoTone("Location", "Scout"), scrollState)
    StarGate {
        Inflated<PostGalaxy>(route) { target ->
            context(koinViewModel<LocationScout> { parametersOf(target) }) { LocationScoutView(scrollState) }
        }
    }
}

/**
 * The view of [LocationScoutRoute], in a column scrolled by [scrollState] under the top bar: the name of the galaxy the
 * location is posted to, if any, the stages of the scout with the one at hand lit, and that stage's form. The cousin
 * of the web's `viewLocationScout`.
 */
@Composable
context(model: LocationScout, star: Star)
fun LocationScoutView(scrollState: ScrollState) {
    val stage by model.stageState.collectAsState()
    val isPosted by model.isPostedState.collectAsState()
    val portal = currentPortal
    // the scout gives its place to where it was headed, as the web's goes on to it
    LaunchedEffect(isPosted) {
        if (isPosted) portal.swap(model.galaxy?.toRoute() ?: HomeRoute)
    }

    ConfigBody(model.galaxy?.name, scrollState) {
        StageHeader(LocationScoutStage.entries.map { it.label }, stage.ordinal)
        when (stage) {
            LocationScoutStage.Search -> context(model.finder) { LocationFinderForm() }
            LocationScoutStage.Edit -> LocationEditStage()
            LocationScoutStage.Review -> LocationReviewStage()
        }
    }
}

/**
 * The edit of the new location in the scout's editor, then the way back to the search, the editor's message, and the
 * way on to the review, once the edit is valid.
 */
@Composable
context(model: LocationScout)
private fun LocationEditStage() {
    Column(2) {
        context(model.editor) { LocationEditForm() }
        Row(1, Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Button("Back", onClick = model::search, style = ButtonStyle.Zen)
            Box(Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) { MessageBox(model.editor.messages) }
            Button("Next", onClick = model::review)
        }
    }
}

/**
 * The location as it will be posted, then the post mode, the way back to the edit or the search, the last message, and
 * the way to post. The cousin of the web's review stage.
 */
@Composable
context(model: LocationScout, star: Star)
private fun LocationReviewStage() {
    val focus by model.focusState.collectAsState()
    val mode by model.feedModeState.collectAsState()
    val isSaving by model.editor.isSavingState.collectAsState()
    val image by model.editor.imageEditor.previewImageState.collectAsState()
    val location = (focus as? LocationScoutFocus.Review)?.location ?: return

    Column(2) {
        when (location) {
            is Location -> context(null as FeedContext?) { FeedRow(location, mode) }
            is LocationEdit -> PostRow(location.copy(image = image), star.username, mode)
        }
        Column(1, Modifier.fillMaxWidth(), horizontalAlignment = Alignment.End) {
            DropMenu(model.postModeState)
            Row(1, Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                when (location) {
                    is LocationEdit -> Button("Edit", onClick = model::resumeEdit, style = ButtonStyle.Zen)
                    is Location -> Button("Back to search", onClick = model::search, style = ButtonStyle.Zen)
                }
                Column(1, Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                    MessageBox(model.editor.messages)
                    MessageBox(model.postMessage)
                }
                Button("Post", onClick = model::post, isEnabled = !isSaving)
            }
        }
    }
}
