package streetlight.web.ui

import kampfire.model.handleResponse
import koala.css.*
import koala.dom.*
import koala.model.storeOf
import koala.utils.jsonPrettyConfig
import streetlight.model.data.LocationConfig

fun ViewScope.viewLocationAutomation(config: LocationConfig) = formColumn {
    formRow {
        eventSchemaSection(config)
    }
}

fun ViewScope.eventSchemaSection(config: LocationConfig) = formSection("Event Schema") {
    val eventSchemaField = storeOf(config.eventSchema)
    val messages = MessageStore()
    when (val eventsUrl = config.location.eventsUrl) {
        null -> textBlock("No calendar url available.")
        else -> column {
            formSubmit("reload schema", {
                launchEffect {
                    messages.deliverSending()
                    val schema = api.parseEventSchema(eventsUrl).handleResponse(messages) ?: return@launchEffect
                    messages.deliver("schema delivered")
                    eventSchemaField.set(schema)
                }
            }, messages)
            flowBlock(eventSchemaField) { eventSchema ->
                textBlock(jsonPrettyConfig.encodeToString(eventSchema), modify(WhiteSpacePreLine))
            }
        }
    }
}