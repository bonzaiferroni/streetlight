package streetlight.web.shells

import koala.css.AlignItemsCenter
import koala.css.AlignItemsStretch
import koala.css.modify
import koala.html.action
import koala.html.button
import koala.html.column
import koala.html.textBlock
import kotlinx.html.FlowContent
import streetlight.model.data.Event
import streetlight.web.EditStoryRoute
import streetlight.web.EventObjectRoute
import streetlight.web.SandboxRoute
import streetlight.web.pages.appFooter

fun FlowContent.hapsTab(events: List<Event>) {
    column {
        events.forEach { event ->
            action(EventObjectRoute(event)) {
                textBlock(event.title)
            }
        }
        button("Create story", EditStoryRoute())
        button("Go to sandbox", SandboxRoute)
        appFooter()
    }
}