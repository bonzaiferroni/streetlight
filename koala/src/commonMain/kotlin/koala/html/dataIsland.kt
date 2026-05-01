package koala.html

import koala.utils.jsonConfig
import kotlinx.html.FlowContent
import kotlinx.html.SCRIPT
import kotlinx.html.script
import kotlinx.html.unsafe

inline fun <reified T> FlowContent.dataIsland(
    id: Id,
    data: T,
    crossinline block: SCRIPT.() -> Unit = {}
) {
    script(type = "application/json") {
        setId(id)

        block()

        unsafe {
            +jsonConfig.encodeToString(data)
        }
    }
}