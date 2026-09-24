package streetlight.web.ui

import streetlight.model.ui.StarRoute
import streetlight.web.model.RouteDockState
import koala.modifier.*
import koala.dom.RouteScope
import koala.dom.ViewScope
import koala.dom.lazyTabs
import koala.dom.section
import streetlight.model.data.ProfileConfig
import streetlight.model.data.Star
import streetlight.model.data.toEdit
import streetlight.model.ui.ProfileConfigRoute

fun ViewScope.viewProfileConfig(star: Star, config: ProfileConfig) {
    val model = app.getProfileEditor(star.toEdit(config.design), contentScope)
    configBody("Profile", "Config", "viewProfileConfig.kt") {
        configHeading(star.username.value, StarRoute(star.username))

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
    }

    dock.mergeState(ProfileConfigRoute, RouteDockState(listOf(StarRoute(star.username)), title = star.username.value))
}

fun RouteScope.viewProfileConfigRoute() {
    starRouteBlock<ProfileConfigRoute, ProfileConfig> { star, config ->
        viewProfileConfig(star, config)
    }
}
