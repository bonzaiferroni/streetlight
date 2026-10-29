package streetlight.compose.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.graphicsLayer
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState

/**
 * Lays out the page of [content] on paper over the theme's lights, below a platform's [bar], such as the desktop
 * window's title bar. The [overlay] floats over the page, and may blur it, map and all, through [LocalPageHaze]; the
 * page keeps [LocalPageInsets] of room for it.
 */
@Composable
fun AppScaffold(
    bar: @Composable () -> Unit = {},
    overlay: @Composable BoxScope.() -> Unit = {},
    content: @Composable () -> Unit,
) {
    val theme = currentTheme
    val pageHaze = rememberHazeState()
    val pageInsets = remember(theme.unit) {
        PaddingValues(top = theme.unit * TopBarUnits, bottom = theme.unit * DockUnits)
    }
    CompositionLocalProvider(LocalPageHaze provides pageHaze, LocalPageInsets provides pageInsets) {
        // the page, paper and lights and all, is what a floating card blurs behind itself; the paper keeps the capture
        // opaque, so the blur covers what lies behind the card
        Column(Modifier.fillMaxSize()) {
            bar()
            // the overlay is the page's sibling, since a blur cannot draw a source it lies within
            Box(Modifier.fillMaxWidth().weight(1f)) {
                Box(Modifier.fillMaxSize().hazeSource(pageHaze).background(theme.colors.paper)) {
                    Spacer(
                        Modifier.fillMaxSize()
                            .graphicsLayer()
                            .drawBehind { drawLights(theme.lights) }
                    )
                    content()
                }
                overlay()
            }
        }
    }
}
