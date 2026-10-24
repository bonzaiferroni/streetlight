package streetlight.web.model

import kotlinx.coroutines.CoroutineScope
import streetlight.model.io.ApiClient

/** Data loaded once per session and cleared at sign-out. */
class DataCache(val api: ApiClient) {

    val galaxyFleet = Fleet { api.galaxy.readUserGalaxies() }

    fun clear() {
        galaxyFleet.clear()
    }
}