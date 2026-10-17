package streetlight.web.view

import koala.dom.ViewScope
import streetlight.model.data.LocationEdit
import streetlight.model.data.Star
import streetlight.web.dom.layout.formRow
import streetlight.web.dom.layout.formSection
import streetlight.web.model.LocationOnboarder
import streetlight.web.model.getLocationEditor
import streetlight.web.model.getLocationScout

context(star: Star)
fun ViewScope.locationOnboarder() {
    val model = app.getLocationOnboarder(contentScope)
    val editor = app.getLocationEditor(LocationEdit(), contentScope)

    formRow {
        formSection("Your Location") {

        }
    }
}