package streetlight.server.daemon.agent

import streetlight.model.data.EventEdit

/** The text this event is embedded from: a `<label>: <value>` line for each field it has that describes it. */
fun EventEdit.getEmbeddingsText(): String = listOfNotNull(
    title?.let { "Title: $it" },
    description?.let { "Description: ${it.value}" },
    website?.let { "Website: ${it.value}" },
    ageMin?.let { "Minimum Age: $it" },
).joinToString("\n")
