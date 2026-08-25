package streetlight.agent

import ai.koog.agents.core.tools.annotations.LLMDescription
import ai.koog.agents.core.tools.annotations.Tool
import ai.koog.agents.core.tools.reflect.ToolSet
import kampfire.model.toDataOrNull

class UrlToolSet : ToolSet {
    @Tool
    @LLMDescription("Read html content from a url")
    suspend fun readHtmlContentFromUrl(url: String): String? {
        return fetchText(url).toDataOrNull()?.text
    }
}
