package streetlight.server.daemon.agent

import kotlinx.serialization.json.Json
import streetlight.model.data.EventEdit

/** The events in the resource [name], each with the tags a person gave it, failing when it is missing. */
fun readEventEdits(name: String): List<EventEdit> {
    val text = EventEdit::class.java.getResource("/$name")?.readText()
        ?: error("Resource $name is missing")
    return Json.decodeFromString(text)
}
