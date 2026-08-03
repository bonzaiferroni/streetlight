package streetlight.web.ui

import kampfire.model.handleResponse
import koala.css.*
import koala.dom.*
import koala.model.storeOf
import koala.utils.jsonPrettyConfig
import streetlight.model.data.LocationConfig
import streetlight.model.data.LocationConfigContent

fun ViewScope.locationAutomationForm(content: LocationConfigContent) = formColumn {
    formRow {
        eventSchemaSection(content)
    }
}

fun ViewScope.eventSchemaSection(content: LocationConfigContent) = formSection("Event Schema") {
    val eventSchemaField = storeOf(content.config.eventSchema)
    val messages = MessageStore()
    when (val eventsUrl = content.location.eventsUrl) {
        null -> textBlock("No calendar url available.")
        else -> column {
            formSubmit("reload schema", {
                launchEffect {
                    messages.deliverSending()
                    val schema = api.parseEventSchema(eventsUrl).handleResponse(messages, "schema delivered")
                        ?: return@launchEffect
                    eventSchemaField.set(schema)
                }
            }, messages)
            flowBlock(eventSchemaField) { eventSchema ->
                if (eventSchema == null) return@flowBlock
                val uploadMessages = MessageStore()
                column {
                    formSubmit("Upload", {
                        launchEffect {
                            api.editLocationConfig(content.config.copy(eventSchema = eventSchemaField.now))
                                .handleResponse(uploadMessages)
                        }
                    }, uploadMessages)
                    textBlock(jsonPrettyConfig.encodeToString(eventSchema), modify(WhiteSpacePreLine))
                }
            }
        }
    }
}