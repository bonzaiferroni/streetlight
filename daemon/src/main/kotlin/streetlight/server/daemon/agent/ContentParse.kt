package streetlight.server.daemon.agent

import kotlinx.serialization.Serializable

/** The result of reading content from a web page, and whether it held the content expected and all of it. */
@Serializable
data class ContentParse<T>(
    val isExpectedContent: Boolean,
    val isIncompleteContent: Boolean,
    val content: T? = null,
)