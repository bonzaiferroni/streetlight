package streetlight.web.model

import koala.model.storeOf
import kotlinx.coroutines.CoroutineScope
import streetlight.model.data.EventEdit
import streetlight.model.data.Galaxy
import streetlight.web.ui.ViewModel

class EventScout(
    val galaxy: Galaxy,
    val editor: EventEditor,
    val location: LocationScout,
    private val scope: CoroutineScope,
) {
    private val state = storeOf(EventScoutState(blankEvent))

    // val location = LocationFinder(scope)
    // val editor = EventEditor(EventEdit(), scope)

}