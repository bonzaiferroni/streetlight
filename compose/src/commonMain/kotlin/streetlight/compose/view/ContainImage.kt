package streetlight.compose.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import coil3.compose.rememberAsyncImagePainter
import koala.Image

/**
 * Shows [image] whole, its `medium` variant contained over a blurred, scaled and dimmed backdrop of itself, which fills
 * the space the image leaves. The variant is read once and drawn twice, so the image costs one request. The cousin of
 * koala's `containImage`.
 */
@Composable
fun ContainImage(image: Image?, modifier: Modifier = Modifier) {
    Box(modifier.clipToBounds()) {
        if (image == null) return@Box
        val painter = rememberAsyncImagePainter(image.medium ?: image.url)
        Image(
            painter = painter,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            colorFilter = BackdropDim,
            modifier = Modifier.matchParentSize()
                .graphicsLayer {
                    scaleX = BackdropScale
                    scaleY = BackdropScale
                }
                .blur(BackdropBlur),
        )
        Image(
            painter = painter,
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

/** The backdrop's scale, its blur, and its brightness: the web's `scale(1.2)` and `blur(24px) brightness(0.8)`. */
private const val BackdropScale = 1.2f
private val BackdropBlur = 24.dp
private val BackdropDim = ColorFilter.colorMatrix(ColorMatrix().apply { setToScale(0.8f, 0.8f, 0.8f, 1f) })
