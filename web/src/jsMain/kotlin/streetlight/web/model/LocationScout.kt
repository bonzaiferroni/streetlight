package streetlight.web.model

import kotlinx.coroutines.CoroutineScope
import streetlight.model.data.Galaxy
import streetlight.model.data.LocationEdit
import streetlight.web.ui.ViewModel

class LocationScout(
    private val scope: CoroutineScope,
    override val app: Streetlight,
    val galaxy: Galaxy,
): ViewModel {

    // val editor = LocationEditor(LocationEdit(), scope, app)
    // val finder = LocationFinderProto(scope, app)
}