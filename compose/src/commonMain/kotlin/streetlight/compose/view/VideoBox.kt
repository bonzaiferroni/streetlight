package streetlight.compose.view

import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import io.github.kdroidfilter.composemediaplayer.VideoPlayerSurface
import io.github.kdroidfilter.composemediaplayer.rememberVideoPlayerState
import koala.Video
import org.koin.compose.koinInject
import streetlight.compose.model.AppEnv
import streetlight.compose.modifier.Outline
import streetlight.compose.modifier.borderRadius

/**
 * [video] playing silently on a loop, at its own shape, in a rounded frame. The web's `videoBox`, whose video plays as
 * its tag does: muted, looping, without controls.
 */
@Composable
fun VideoBox(video: Video, modifier: Modifier = Modifier) {
    val env = koinInject<AppEnv>()
    val player = rememberVideoPlayerState()
    LaunchedEffect(player, video) {
        player.volume = 0f
        player.loop = true
        player.openUri(env.urlOf(video))
    }
    VideoPlayerSurface(
        player,
        // the shape is the video's once it is known
        modifier.fillMaxWidth()
            .aspectRatio(player.aspectRatio.takeIf { it > 0f } ?: DefaultRatio)
            .borderRadius(2, moonShadow = true, outline = Outline.Mid),
    )
}

/** The address of [video], on the server when its URL is relative. */
private fun AppEnv.urlOf(video: Video) = if (video.url.isRelative) apiUrl + video.url.value else video.url.value

/** The shape of a video before its own is known. */
private const val DefaultRatio = 16f / 9f
