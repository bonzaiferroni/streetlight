package streetlight.web.ui

import koala.css.*
import koala.dom.*
import streetlight.web.model.EventScout

fun ViewScope.eventSearchForm(model: EventScout) = formSectionLegacy("Add an event") {
    formCard {
        formPart(
            instructions = "What is the name of the event?",
            bullets = listOf("The event may already be on Streetlight and you can post it to ${model.galaxy.name}.")
        ) {
            row {
                textField(model.query, "name", modify(Flex1))
                button("create", model::create)
            }

            selectionBlock(model.queryEventsFlow, model.event) { event ->
                textBlock(event.title)
            }
        }
    }
}

