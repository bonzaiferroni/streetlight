package streetlight.web.ui

import koala.modifier.*
import koala.dom.RouteScope
import koala.dom.ViewScope
import koala.dom.lazyTabs
import koala.dom.section
import koala.html.filigree
import koala.html.heading2
import streetlight.model.data.ProfileConfig
import streetlight.model.data.Star
import streetlight.model.data.toEdit
import streetlight.model.ui.ProfileConfigRoute
import streetlight.web.shells.starRouteMenu

fun ViewScope.viewProfileConfig(star: Star, config: ProfileConfig) {
    val model = app.getProfileEditor(star.toEdit(config.design), contentScope)
    section(modify(FlexColumn)) {
        filigree {
            heading2("Profile Config", modify(TextAlignCenter))
        }

        lazyTabs {
            tab("Content") {
                starProfileForm(model)
            }
            tab("Theme") {
                themeForm(model.designer.theme)
            }
            tab("Layout") {
                layoutBuilder(model.designer.layout)
            }
        }
        formSubmit("Save", model::submit, model.messages)

        appFooter(sourcePathUi("viewProfileConfig.kt"))

        starRouteMenu(star, ProfileConfigRoute, true)
    }
}

fun RouteScope.viewProfileConfigRoute() {
    starRouteBlock<ProfileConfigRoute, ProfileConfig> { star, config ->
        viewProfileConfig(star, config)
    }
}
