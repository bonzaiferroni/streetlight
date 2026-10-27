package streetlight.compose.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import streetlight.model.io.ApiClient

/** The events map: its [markerMap], filled view by view by its [cache]. The cousin of the web's `Earth`. */
class EarthModel(api: ApiClient, geoMap: GeoMap) : ViewModel() {
    val markerMap = MarkerMap(geoMap)
    val cache = EarthCache(viewModelScope, api, geoMap, markerMap)
}
