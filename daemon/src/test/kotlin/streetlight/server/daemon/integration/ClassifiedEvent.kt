package streetlight.server.daemon.integration

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import streetlight.model.data.EventEdit
import streetlight.model.data.EventType

/** An observed [event] paired with the [eventType] a person gave it. */
@Serializable
data class ClassifiedEvent(
    val event: EventEdit,
    val eventType: EventType,
)

/** The classified events in the test resource [name], failing when it is missing. */
fun readClassifiedEvents(name: String): List<ClassifiedEvent> {
    val text = ClassifiedEvent::class.java.getResource("/$name")?.readText()
        ?: error("Test resource $name is missing")
    return Json.decodeFromString(text)
}
