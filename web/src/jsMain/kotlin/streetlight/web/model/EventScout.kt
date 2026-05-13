package streetlight.web.model

import kotlinx.coroutines.CoroutineScope
import streetlight.model.data.EventEdit
import streetlight.model.data.Galaxy
import streetlight.web.ui.ViewModel

class EventScout(
    override val app: Streetlight,
    val scope: CoroutineScope,
    val galaxy: Galaxy,
): ViewModel {
    // private val state = storeOf(EventScoutState(blankEvent))

    val location = LocationFinder(scope, app)
    val editor = EventEditor(EventEdit(), scope, app.client)


}