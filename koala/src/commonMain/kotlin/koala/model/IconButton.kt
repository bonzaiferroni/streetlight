package koala.model

import koala.Svg
import koala.SvgFile
import koala.component.icon
import koala.modifier.Modifier
import kotlinx.html.BUTTON

/** An icon that either navigates or acts. */
sealed interface IconButton

/** An icon that navigates to [route]. */
data class IconRoute(
    val svg: Svg,
    val route: WebRoute
): IconButton

/**
 * An icon that runs [onClick], for the few icons as clear as a label, such as a cross that closes. It is announced as
 * [ariaLabel], since it shows no text.
 */
data class IconAction(
    val svg: Svg,
    val ariaLabel: String,
    val mod: Modifier? = null,
    val config: BUTTON.() -> Unit = { },
    val onClick: () -> Unit,
): IconButton {
    companion object {
        fun close(onClick: () -> Unit) = IconAction(SvgFile.X.small, "close", onClick = onClick)
    }
}