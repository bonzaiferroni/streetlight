package streetlight.compose.view

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf

/** The room a page keeps at its edges for the top bar and the [RouteDock], which float over it. */
val LocalPageInsets = staticCompositionLocalOf<PaddingValues> { error("No page insets provided") }

val currentPageInsets: PaddingValues
    @Composable get() = LocalPageInsets.current
