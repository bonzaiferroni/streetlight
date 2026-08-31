package streetlight.web.ui

import koala.dom.*
import streetlight.model.data.LocationConfigContent

fun ViewScope.locationAutomationForm(content: LocationConfigContent) = formColumn {
    formRow {
        eventSchemaSection(content)
    }
}

fun ViewScope.eventSchemaSection(content: LocationConfigContent) = formSection("Event Schema") {
    // val schemasField = storeOf(content.origins.firstOrNull()?.schemas?.map { it.selector } ?: emptyList())
    // val messages = MessageStore()
    // when (val eventsUrl = content.location.eventsUrl) {
    //     null -> textBlock("No calendar url available.")
    //     else -> column {
    //         formSubmit("reload schema", {
    //             launchEffect {
    //                 messages.deliverSending()
    //                 val schemas = api.parseEventSchema(eventsUrl).handleResponse(messages, "schema delivered")
    //                     ?: return@launchEffect
    //                 schemasField.addAll(schemas)
    //             }
    //         }, messages)
    //         flowBlock(schemasField) { eventSchema ->
    //             val uploadMessages = MessageStore()
    //             column {
    //                 formSubmit("Upload", {
    //                     launchEffect {
    //                         api.uploadSchemas(UrlSchemas(content.location.locationId, eventsUrl, schemasField.now))
    //                             .handleResponse(uploadMessages)
    //                     }
    //                 }, uploadMessages)
    //                 textBlock(jsonPrettyConfig.encodeToString(eventSchema), modify(WhiteSpacePreLine))
    //             }
    //         }
    //     }
    // }
}