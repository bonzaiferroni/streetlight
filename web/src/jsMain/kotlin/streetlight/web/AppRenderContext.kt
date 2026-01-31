package streetlight.web

import koala.dom.AltRenderContext
import koala.dom.DOMContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.dom.addClass
import kotlinx.dom.clear
import kotlinx.dom.removeClass
import kotlinx.html.dom.append
import kotlinx.html.*
import org.w3c.dom.HTMLElement

class AltAppRenderContext(
    consumer: DOMContext,
    override val renderScope: CoroutineScope,
    val app: AppContext,
): DOMContext by consumer, AppContext by app, AltRenderContext {
}