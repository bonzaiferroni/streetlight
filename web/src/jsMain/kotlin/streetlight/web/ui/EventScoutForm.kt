package streetlight.web.ui

import koala.css.*
import koala.dom.*
import streetlight.web.model.EventScout
import streetlight.web.model.LocationScout

fun RenderContext.eventSearchForm(model: EventScout) = formCard {
    formPart(
        instructions = "What is the name of the event?",
        bullets = listOf("The event may already be on Streetlight and you can post it to ${model.galaxy.name}.")
    ) {
        row {
            textField("name", modify(Flex1), model::setQuery, model.queryFlow)
            button("create", onClick = model::createFromQuery)
        }

        selectionBlock(model.queryEventsFlow, model::setEvent, model.eventFlow) { event ->
            textBlock(event.title)
        }
    }
}

