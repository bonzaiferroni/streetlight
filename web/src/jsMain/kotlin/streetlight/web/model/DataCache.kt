package streetlight.web.model

import kotlinx.coroutines.CoroutineScope
import streetlight.web.io.ApiClient

class DataCache(val api: ApiClient) {

    val galaxyFleet = Fleet { api.readUserGalaxies() }

    fun clear() {
        galaxyFleet.clear()
    }
}