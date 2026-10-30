package streetlight.compose.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import streetlight.model.data.GalaxyContent
import streetlight.model.io.ApiClient

/** A galaxy's [content]: the galaxy, and its [feed]. */
class GalaxyModel(val content: GalaxyContent, api: ApiClient, config: AppConfig) : ViewModel() {
    val feed = FeedModel(viewModelScope, api, config.feedModeState, content.feed)
}
