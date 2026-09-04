package streetlight.web.ui

import koala.css.FlexColumn
import koala.css.TextAlignCenter
import koala.css.modify
import koala.dom.RouteScope
import koala.dom.ViewScope
import koala.dom.section
import koala.dom.tabs
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

        tabs {
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

        appFooter("")

        starRouteMenu(star, ProfileConfigRoute, true)
    }
}

fun RouteScope.viewProfileConfigRoute() {
    starRouteBlock<ProfileConfigRoute, ProfileConfig> { star, config ->
        viewProfileConfig(star, config)
    }
}
