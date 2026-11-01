package streetlight.compose.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import streetlight.model.data.LocationContent
import koala.model.Toaster
import streetlight.model.io.ApiClient

/** A location's [content]: the location, and the [feed] of its events. */
class LocationModel(val content: LocationContent, api: ApiClient, toaster: Toaster, config: AppConfig) : ViewModel() {
    val feed = FeedModel(viewModelScope, api, toaster, config.feedModeState, content.feed)
}
