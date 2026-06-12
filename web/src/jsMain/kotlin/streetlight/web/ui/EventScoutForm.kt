package streetlight.web.ui

import koala.css.*
import koala.dom.*
import streetlight.web.model.EventScout

fun RenderScope.eventSearchForm(model: EventScout) = formSection("Add an event") {
    formCard {
        formPart(
            instructions = "What is the name of the event?",
            bullets = listOf("The event may already be on Streetlight and you can post it to ${model.galaxy.name}.")
        ) {
            row {
                textField("name", modify(Flex1), model::setQuery, model.queryFlow)
                button("create", onClick = model::create)
            }

            selectionBlock(model.queryEventsFlow, model::setEvent, model.eventFlow) { event ->
                textBlock(event.title)
            }
        }
    }
}

