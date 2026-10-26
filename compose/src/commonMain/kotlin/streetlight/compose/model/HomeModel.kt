package streetlight.compose.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import streetlight.model.data.HomeContent
import streetlight.model.io.ApiClient

/** The home [content]: its top galaxies, and its [feed]. */
class HomeModel(val content: HomeContent, api: ApiClient, settings: AppSettings) : ViewModel() {
    val feed = FeedModel(viewModelScope, api, settings.feedModeState, content.feed)
}
