package streetlight.compose.view

import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import koala.SvgPack
import koala.SvgFile
import streetlight.compose.model.AppConfigRoute
import streetlight.compose.model.CityFeedRoute
import streetlight.compose.model.EarthRoute
import streetlight.compose.model.currentPortal
import streetlight.compose.modifier.filigree
import streetlight.compose.modifier.padding
import streetlight.compose.modifier.pointerClick

/**
 * The site's menu: the [helm] beside the logo, then the site's places, each closing the menu with [onClose] as it
 * opens its route. A place the app has no route for yet does nothing. The cousin of the
 * web's site menu.
 */
@Composable
fun HelmMenu(onClose: () -> Unit, helm: @Composable () -> Unit) {
    val theme = currentTheme
    val portal = currentPortal

    // as wide as its widest item, which the labels' filigrees stretch to
    Column(1, Modifier.width(IntrinsicSize.Max).padding(1)) {
        Row(1, verticalAlignment = Alignment.CenterVertically) {
            helm()
            TextLogo(
                Modifier.height(theme.unit * LogoUnits).pointerClick {
                    portal.home()
                    onClose()
                }
            )
        }
        HelmItem("Earth", SvgFile.Earth, isCurrent = portal.dockRoute == EarthRoute) {
            portal.dock(EarthRoute)
            onClose()
        }
        HelmItem("Galaxies", SvgFile.Satellite)
        HelmItem("Cities", SvgFile.City, isCurrent = portal.backStack.last() == CityFeedRoute) {
            portal.go(CityFeedRoute)
            onClose()
        }
        HelmLabel("meta")
        HelmItem("Help & Feedback", SvgFile.Question)
        HelmItem("Contribute", SvgFile.HeartHandshake)
        HelmItem("Status", SvgFile.Chart)
        HelmItem("Privacy", SvgFile.EyeClosed)
        HelmLabel("config")
        HelmItem("Settings", SvgFile.Gear, isCurrent = portal.backStack.last() == AppConfigRoute) {
            portal.go(AppConfigRoute)
            onClose()
        }
        HelmItem("Theme", SvgFile.Sun)
    }
}

/** A place in the menu, [label] beside the large icon of [svg], in the primary color while [isCurrent], opening it with [onClick] when given. */
@Composable
private fun HelmItem(label: String, svg: SvgPack, isCurrent: Boolean = false, onClick: (() -> Unit)? = null) {
    val theme = currentTheme
    val color = if (isCurrent) theme.colors.primaryFg else theme.colors.ink
    Row(
        1,
        onClick?.let { Modifier.pointerClick(it) } ?: Modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(svg.large, color = { color }, size = theme.unit * HelmIconUnits)
        BasicText(label, style = theme.text.regular, color = { color })
    }
}

/** A heading of a group of places, [label] small and faint in a filigree. */
@Composable
private fun HelmLabel(label: String) {
    val theme = currentTheme
    BasicText(
        label.uppercase(),
        Modifier.fillMaxWidth().filigree(),
        style = theme.text.small,
        color = { theme.colors.ink.copy(alpha = theme.opacity.half) },
    )
}

/** The height of the logo in units, the web's `Height(5)`. */
private const val LogoUnits = 5
