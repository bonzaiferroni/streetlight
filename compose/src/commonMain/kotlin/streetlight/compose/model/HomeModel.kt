package streetlight.compose.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import streetlight.model.data.HomeContent
import streetlight.model.io.ApiClient

/** The home [content]: its top galaxies, its [feed], and the [markerMap] of the feed's entities. */
class HomeModel(val content: HomeContent, api: ApiClient, settings: AppSettings, geoMap: GeoMap) : ViewModel() {
    val feed = FeedModel(viewModelScope, api, settings.feedModeState, content.feed)
    val markerMap = MarkerMap(geoMap)

    init {
        viewModelScope.launch { feed.feedState.flow.collect { markerMap.setPoints(it.entities) } }
    }
}
