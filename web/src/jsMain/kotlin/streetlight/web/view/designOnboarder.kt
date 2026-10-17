package streetlight.web.view

import koala.dom.ViewScope
import koala.dom.component.column
import koala.dom.layout.lazyTabs
import koala.model.MessageStore
import streetlight.model.data.DefaultLayout
import streetlight.model.data.PageDesign
import streetlight.web.dom.layout.formSubmit
import streetlight.web.layout.BodyStyle
import streetlight.web.model.DesignEditor
import streetlight.web.model.LocationOnboarder
import streetlight.web.model.api

/** The design of the onboarded location: its theme and layout, held here until saved. */
context(model: LocationOnboarder)
fun ViewScope.designOnboarder() {
    val designer = DesignEditor(api, DefaultLayout.location, PageDesign(null, null))
    val messages = MessageStore()

    column(BodyStyle.MainColumn) {
        lazyTabs {
            tab("theme") {
                context(designer.theme) {
                    themeForm()
                }
            }
            tab("layout") {
                context(designer.layout) {
                    layoutBuilder()
                }
            }
        }
        formSubmit("Save design", { model.submitDesign(designer, messages) }, messages)
    }
}
