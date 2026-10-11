package streetlight.server.daemon.agent

import streetlight.model.data.EventEdit
import streetlight.model.data.LocationEdit

/**
 * The text this event is embedded from: a `<label>: <value>` line for each field it has that describes it, and for the
 * schema.org [ldType] its page gave it, its words split apart.
 */
fun EventEdit.getEmbeddingsText(ldType: String? = null): String = listOfNotNull(
    ldType?.let { "Category: ${it.replace(camelBoundary, " ")}" },
    title?.let { "Title: $it" },
    description?.let { "Description: ${it.value}" },
    website?.let { "Website: ${it.value}" },
    ageMin?.let { "Minimum Age: $it" },
).joinToString("\n")

/**
 * The text this location is embedded from: a `<label>: <value>` line for each field it has that describes it, led by
 * the category and type the map gives it.
 */
fun LocationEdit.getEmbeddingsText(): String = listOfNotNull(
    mapType?.let { type -> "Category: ${listOfNotNull(mapCategory, type).joinToString(", ")}" },
    name?.let { "Name: $it" },
    description?.let { "Description: ${it.value}" },
    website?.let { "Website: ${it.value}" },
).joinToString("\n")

private val camelBoundary = Regex("(?<=[a-z])(?=[A-Z])")
