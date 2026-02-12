package streetlight.web.shells

import koala.css.AlignItemsStretch
import koala.css.BorderRadius1
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
import streetlight.web.EditEventRoute

fun FlowContent.eventProfileShell(event: Event) {
    column(modify(AlignItemsStretch)) {
        val imageUrl = event.imageUrl
        if (imageUrl != null) {
            image(imageUrl, modify(BorderRadius1))
        }
        heading1(event.title)

        tabs(Id("event-tabs")) {
            tab("Profile") {
                textBlock("[Event information]")
                action(EditEventRoute) {
                    button("edit")
                }
            }
            tab("Requests") {
                textBlock("[Requests information]")
            }
        }
    }
}