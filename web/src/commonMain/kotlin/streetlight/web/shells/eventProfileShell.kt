package streetlight.web.shells

import koala.css.AlignItemsStretch
import koala.css.BorderRadius1
import koala.css.MarginAuto
import koala.css.MaxHeight64
import koala.css.modify
import koala.html.Id
import koala.html.action
import koala.html.button
import koala.html.column
import koala.html.heading1
import koala.html.image
import koala.html.tab
import koala.html.tabs
import koala.html.textBlock
import kotlinx.html.FlowContent
import streetlight.model.data.Event
import streetlight.web.EditEventIdRoute

fun FlowContent.eventProfileShell(event: Event) {
    column(EventProfileShell.id, modify(AlignItemsStretch)) {
        val imageUrl = event.imageUrl
        if (imageUrl != null) {
            image(imageUrl, modify(BorderRadius1, MaxHeight64, MarginAuto))
        }
        heading1(event.title)

        tabs(EventProfileShell.tabsId) {
            tab("Profile") {
                textBlock("[Event information]")
                action(EditEventIdRoute(event.eventId)) {
                    button("edit")
                }
            }
            tab("Requests") {
                textBlock("[Requests information]")
            }
        }
    }
}

object EventProfileShell {
    val id = Id("event-profile")
    val tabsId = Id("event-tabs")
}