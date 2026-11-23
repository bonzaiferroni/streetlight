package streetlight.web.model

import kotlinx.coroutines.CoroutineScope
import streetlight.model.music.text.NotationStyle
import streetlight.web.utility.localStoreOf

class MusicConfig(
    private val scope: CoroutineScope,
) {
    val notationStyleState = localStoreOf(scope, NOTATION_STYLE_KEY, NotationStyle.Letters)
    val notationViewState = localStoreOf(scope, NOTATION_VIEW_KEY, NotationView.Both)
    val isLyricsShownState = localStoreOf(scope, LYRICS_SHOWN_KEY, true)
    val isClickOnState = localStoreOf(scope, CLICK_ON_KEY, true)
}

private const val NOTATION_STYLE_KEY = "streetlight.notation-style"
private const val NOTATION_VIEW_KEY = "streetlight.notation-view"
private const val LYRICS_SHOWN_KEY = "streetlight.lyrics-shown"
private const val CLICK_ON_KEY = "streetlight.click-on"