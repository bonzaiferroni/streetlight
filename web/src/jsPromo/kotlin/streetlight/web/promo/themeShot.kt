package streetlight.web.promo

import koala.dom.component.box
import koala.dom.component.column
import koala.modifier.*
import kotlinx.html.js.div
import streetlight.web.component.textLogo
import streetlight.web.layout.BodyStyle
import streetlight.web.model.ThemeEditor
import streetlight.web.view.themeForm

/** The logo over the theme form, whose lights and glow settings light the page. */
val themeShot = PromoShot {
    column(modify(BodyStyle.MainColumn, Padding(4))) {
        box(AlignSelfCenter) {
            div {
                textLogo(Height(8))
            }
        }
        context(ThemeEditor(null)) {
            themeForm()
        }
    }
}
