package streetlight.web.view

import streetlight.model.ui.StarRoute
import koala.modifier.*
import koala.dom.RouteScope
import koala.dom.ViewScope
import koala.dom.layout.lazyTabs
import koala.dom.component.section
import streetlight.model.data.ProfileConfig
import streetlight.model.data.Star
import streetlight.model.data.toEdit
import streetlight.model.ui.ProfileConfigRoute
import streetlight.web.dom.layout.configBody
import streetlight.web.dom.component.configHeading
import streetlight.web.dom.layout.formSubmit
import streetlight.web.model.getProfileEditor

context(star: Star)
fun ViewScope.viewProfileConfig(config: ProfileConfig) {
    val model = app.getProfileEditor(star.toEdit(config.design), contentScope)
    configBody("Profile", "Config", "viewProfileConfig.kt") {
        configHeading(star.username.value, StarRoute(star.username))

        lazyTabs {
            tab("Content") {
                context(model) {
                    starProfileForm()
                }
            }
            tab("Theme") {
                context(model.designer.theme) {
                    themeForm()
                }
            }
            tab("Layout") {
                context(model.designer.layout) {
                    layoutBuilder()
                }
            }
        }
        formSubmit("Save", model::submit, model.messages)
    }

}

fun RouteScope.viewProfileConfigRoute() {
    starRouteBlock<ProfileConfigRoute, ProfileConfig> { config ->
        viewProfileConfig(config)
    }
}
