package streetlight.server.daemon.agent

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import streetlight.model.data.EventEdit
import streetlight.model.data.EventSubtype
import streetlight.model.data.EventType

/** An observed [event] paired with the [eventType] and [eventSubtype] a person gave it. */
@Serializable
data class ClassifiedEvent(
    val event: EventEdit,
    val eventType: EventType,
    val eventSubtype: EventSubtype? = null,
)

/** The classified events in the resource [name], failing when it is missing. */
fun readClassifiedEvents(name: String): List<ClassifiedEvent> {
    val text = ClassifiedEvent::class.java.getResource("/$name")?.readText()
        ?: error("Resource $name is missing")
    return Json.decodeFromString(text)
}
