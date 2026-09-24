package streetlight.web.ui

import koala.dom.AppendScope
import koala.dom.navigation
import koala.html.AppRoute
import koala.html.heading1
import koala.modifier.*
import kotlinx.html.A

/** The heading of a [configBody]: [title], linking to [route], the page of what the view configures. */
fun AppendScope.configHeading(
    title: String,
    route: AppRoute,
    mod: Modifier? = null,
    config: A.() -> Unit = { },
) = navigation(route, mod) {
    config()
    heading1(title, modify(MoonShadowText, TextAlignCenter))
}
