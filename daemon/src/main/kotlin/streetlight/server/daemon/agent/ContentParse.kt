package streetlight.server.daemon.agent

import kotlinx.serialization.Serializable

/**
 * The result of reading content from a page fetched without scripting, and whether it held the content expected and
 * whether that content needs scripting to appear.
 */
@Serializable
data class BasicContentParse<T>(
    val isExpectedContent: Boolean,
    val isScriptingRequired: Boolean,
    val content: T? = null,
)

/** The result of reading content from a page fetched with scripting, and whether it held the content expected. */
@Serializable
data class ScriptingContentParse<T>(
    val isExpectedContent: Boolean,
    val content: T? = null,
)
