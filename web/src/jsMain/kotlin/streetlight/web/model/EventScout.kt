package streetlight.web.model

import kotlinx.coroutines.CoroutineScope
import streetlight.model.data.EventEdit
import streetlight.model.data.Galaxy
import streetlight.web.ui.ViewModel

class EventScout(
    val galaxy: Galaxy,
    val scope: CoroutineScope,
) {
    // private val state = storeOf(EventScoutState(blankEvent))

    // val location = LocationFinder(scope)
    // val editor = EventEditor(EventEdit(), scope)


}