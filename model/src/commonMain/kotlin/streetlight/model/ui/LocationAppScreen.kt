package streetlight.model.ui

import kampfire.utils.pascalToKebabCase
import koala.model.WebRoute
import koala.model.AppScreen
import koala.model.RouteParse
import koala.model.StaticParse

/** The screens of a LocationApp, the site a location serves at its subdomain. */
enum class LocationAppScreen(
    override val routeParse: RouteParse,
    override val pathRoot: String,
    override val hasShell: Boolean = false,
): AppScreen {
    Home(StaticParse { LocationAppRoute }, "", true);

    override val pathBase = "/$pathRoot"
    override val screenId = "location-app-${name.pascalToKebabCase()}"

    companion object {
        /** The screens a LocationApp's portal parses, its own and those it shares with the app. */
        val portalScreens: List<AppScreen> = entries + Screen.Event
    }
}

/** The home of a LocationApp: the location's page. */
data object LocationAppRoute: WebRoute {
    override val screen get() = LocationAppScreen.Home
    override val title get() = "Home"
    override val origin get() = ""
}
