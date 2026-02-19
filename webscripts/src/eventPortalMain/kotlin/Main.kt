import koala.core.get
import koala.core.onClick
import koala.core.onClickElementAll
import koala.core.queryAttribute
import kotlinx.browser.document
import org.w3c.dom.HTMLElement
import streetlight.model.data.EventId
import streetlight.model.data.SongId
import streetlight.web.ApiClient
import streetlight.web.pages.EventPortalSelector

fun main() {
    val eventId = document.queryAttribute(EventPortalSelector.eventIdAttribute)?.let { EventId(it) } ?: return
    val api = ApiClient()
    val model = EventPortal(eventId)
    console.log("loading event portal")
    document.onClickElementAll(EventPortalSelector.requestItem, model::clickRequest)
    document.onClick(EventPortalSelector.sendRequestButtonId, model::sendRequest)
}

fun EventPortal.clickRequest(element: HTMLElement) {
    val songId = element.attributes[EventPortalSelector.songIdAttribute]?.let { SongId(it) } ?: return
    setSongId(songId)
}