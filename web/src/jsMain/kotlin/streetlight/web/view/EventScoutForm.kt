package streetlight.web.view

import koala.modifier.*
import koala.dom.*
import streetlight.web.model.EventScout
import koala.dom.component.*
import koala.dom.layout.*
import streetlight.web.dom.layout.formCard
import streetlight.web.dom.layout.formPart
import streetlight.web.dom.layout.formSectionLegacy

context(model: EventScout)
fun ViewScope.eventSearchForm() = formSectionLegacy("Add an event") {
    formCard {
        formPart(
            instructions = "What is the name of the event?",
            bullets = listOf("The event may already be on Streetlight.")
        ) {
            row {
                textField(model.query, "name", Flex1)
                button("create", model::create)
            }

            selectorBlock(model.queryEventsFlow, model.event) { event ->
                textBlock(event.title)
            }
        }
    }
}

