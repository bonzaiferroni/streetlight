package streetlight.server.daemon.agent

import streetlight.model.data.EventEdit

/**
 * The text this event is embedded from: a `<label>: <value>` line for each field it has that describes it, and for the
 * schema.org [declaredType] its page gave it, its words split apart.
 */
fun EventEdit.getEmbeddingsText(declaredType: String? = null): String = listOfNotNull(
    declaredType?.let { "Category: ${it.replace(camelBoundary, " ")}" },
    title?.let { "Title: $it" },
    description?.let { "Description: ${it.value}" },
    website?.let { "Website: ${it.value}" },
    ageMin?.let { "Minimum Age: $it" },
).joinToString("\n")

private val camelBoundary = Regex("(?<=[a-z])(?=[A-Z])")
