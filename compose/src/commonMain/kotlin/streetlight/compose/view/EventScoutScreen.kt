package streetlight.compose.view

import streetlight.model.ui.TopBar
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import koala.SvgFile
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import streetlight.compose.model.EventScout
import streetlight.compose.model.EventScoutFocus
import streetlight.compose.model.EventScoutRoute
import streetlight.compose.model.EventScoutStage
import streetlight.compose.model.HomeRoute
import streetlight.compose.model.PostGalaxy
import streetlight.compose.model.currentPortal
import streetlight.compose.model.toRoute
import streetlight.compose.utility.collectAsState
import streetlight.model.data.Location
import streetlight.model.data.Star

/**
 * The screen of [route]: its name as its top bar, and, while a star is signed in, the galaxy the event goes to once
 * read, if any, scouted in the [EventScoutView].
 */
@Composable
fun EventScoutScreen(route: EventScoutRoute) {
    val scrollState = rememberScrollState()
    TopBarMount(TopBar.TwoTone("Event", "Scout"), scrollState)
    StarGate {
        Inflated<PostGalaxy>(route) { target ->
            context(koinViewModel<EventScout> { parametersOf(target) }) { EventScoutView(scrollState) }
        }
    }
}

/**
 * The view of [EventScoutRoute], in a column scrolled by [scrollState] under the top bar: the name of the galaxy the
 * event is posted to, if any, the stages of the scout with the one at hand lit, and the form of the focus. The cousin
 * of the web's `viewEventScout`.
 */
@Composable
context(model: EventScout, star: Star)
fun EventScoutView(scrollState: ScrollState) {
    val focus by model.focusState.collectAsState()
    val stage by model.stageState.collectAsState()
    val isPosted by model.isPostedState.collectAsState()
    val portal = currentPortal
    // the scout gives its place to where it was headed, as the web's goes on to it
    LaunchedEffect(isPosted) {
        if (isPosted) portal.swap(model.galaxy?.toRoute() ?: HomeRoute)
    }

    ConfigBody(model.galaxy?.name, scrollState) {
        StageHeader(EventScoutStage.entries.map { it.label }, stage.ordinal)
        when (val focus = focus) {
            null -> context(model.finder) { LocationFinderForm() }
            EventScoutFocus.LocationEdit -> EventLocationEditStage()
            is EventScoutFocus.EventSearch -> EventSearchStage(focus.location)
            is EventScoutFocus.EventEdit -> EventEditStage()
            is EventScoutFocus.Review -> EventReviewStage(focus.location)
        }
    }
}

/** The edit of the event's new location, then the way back to the search, the editor's message, and the way on. */
@Composable
context(model: EventScout)
private fun EventLocationEditStage() {
    val isSaving by model.locationEditor.isSavingState.collectAsState()
    Column(2) {
        context(model.locationEditor) { LocationEditForm() }
        Row(1, Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Button("Back", onClick = model::searchLocations, style = ButtonStyle.Zen)
            Box(Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) { MessageBox(model.locationEditor.messages) }
            Button("Next", onClick = model::submitLocation, isEnabled = !isSaving)
        }
    }
}

/**
 * The search for an event at [location] by its name, which may already be on Streetlight, and the way to start it
 * there. The cousin of the web's `eventSearchForm`.
 */
@Composable
context(model: EventScout)
private fun EventSearchStage(location: Location) {
    Column(2) {
        FormCard("Add an event") {
            ColumnRow {
                FormSection("What is the name of the event at ${location.label}?") {
                    Row(1, Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        TextField(model.eventQueryState, "name", Modifier.weight(1f), icon = SvgFile.Point)
                        Button("Create", onClick = model::createEvent, style = ButtonStyle.Accent)
                    }
                    Placeholder("The events already on Streetlight")
                }
            }
        }
        Button("Back", onClick = model::searchLocations, style = ButtonStyle.Zen)
    }
}

/** The edit of the new event, then the way back to the event search, the editor's message, and the way on. */
@Composable
context(model: EventScout)
private fun EventEditStage() {
    Column(2) {
        context(model.eventEditor) { EventEditorForm() }
        Row(1, Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Button("Back", onClick = model::searchEvents, style = ButtonStyle.Zen)
            Box(Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) { MessageBox(model.eventEditor.messages) }
            Button("Next", onClick = model::review)
        }
    }
}

/**
 * The event as it will be posted at [location], then the post mode, the way back to the edit, the last message, and
 * the way to post.
 */
@Composable
context(model: EventScout, star: Star)
private fun EventReviewStage(location: Location) {
    val edit by model.eventEditor.editState.collectAsState()
    val image by model.eventEditor.imageEditor.previewImageState.collectAsState()
    val mode by model.feedModeState.collectAsState()
    val isSaving by model.eventEditor.isSavingState.collectAsState()

    Column(2) {
        PostRow(edit.copy(image = image), location, star.username, mode)
        Column(1, Modifier.fillMaxWidth(), horizontalAlignment = Alignment.End) {
            DropMenu(model.postModeState)
            Row(1, Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Button("Edit", onClick = model::resumeEdit, style = ButtonStyle.Zen)
                Column(1, Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                    MessageBox(model.eventEditor.messages)
                    MessageBox(model.postMessage)
                }
                Button("Post", onClick = model::post, isEnabled = !isSaving)
            }
        }
    }
}
