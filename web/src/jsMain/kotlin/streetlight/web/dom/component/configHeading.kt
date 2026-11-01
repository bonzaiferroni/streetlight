package streetlight.web.dom.component

import koala.dom.AppendScope
import koala.dom.component.navigation
import koala.model.WebRoute
import koala.component.heading1
import koala.modifier.*
import kotlinx.html.A
import streetlight.web.dom.layout.mainBody

/** The heading of a [mainBody]: [title], linking to [route], the page of what the view configures. */
fun AppendScope.configHeading(
    title: String,
    route: WebRoute,
    mod: Modifier? = null,
    config: A.() -> Unit = { },
) = navigation(route, mod) {
    config()
    heading1(title, modify(MoonShadowText, TextAlignCenter))
}
