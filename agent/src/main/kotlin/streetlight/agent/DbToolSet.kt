package streetlight.agent

import ai.koog.agents.core.tools.annotations.LLMDescription
import ai.koog.agents.core.tools.annotations.Tool
import ai.koog.agents.core.tools.reflect.ToolSet
import streetlight.model.data.EventEdit

class DbToolSet : ToolSet {
    @Tool
    @LLMDescription("Read html content from a url")
    suspend fun addEvent() {
        val event = EventEdit()
        // val dao = EventTableDao()
        // dao.createEvent()
    }
}