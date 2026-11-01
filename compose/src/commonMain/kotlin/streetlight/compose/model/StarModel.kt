package streetlight.compose.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import streetlight.model.data.StarContent
import koala.model.Toaster
import streetlight.model.io.ApiClient

/** A star's [content]: the star, their skills, and the [feed] of their media. */
class StarModel(val content: StarContent, api: ApiClient, toaster: Toaster, config: AppConfig) : ViewModel() {
    val feed = FeedModel(viewModelScope, api, toaster, config.feedModeState, content.feed)
}
