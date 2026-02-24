package streetlight.web.shells

import koala.css.Width100
import koala.css.modify
import koala.html.action
import koala.html.button
import koala.html.column
import koala.html.textBlock
import kotlinx.html.FlowContent
import streetlight.model.data.Event
import streetlight.web.ChatRoute
import streetlight.web.CreateEventRoute
import streetlight.web.EditPostRoute
import streetlight.web.EventObjectRoute
import streetlight.web.SandboxRoute
import streetlight.web.pages.appFooter

fun FlowContent.hapsTab(events: List<Event>) {
    column(modify(Width100)) {
        events.forEach { event ->
            action(EventObjectRoute(event)) {
                textBlock(event.title)
            }
        }

        button("Create Event", CreateEventRoute)
        button("Create story", EditPostRoute())
        button("Chat", ChatRoute)
        button("Go to sandbox", SandboxRoute)
        appFooter()
    }
}