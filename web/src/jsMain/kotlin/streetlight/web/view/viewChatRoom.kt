package streetlight.web.view

import koala.modifier.*
import koala.dom.*
import kotlinx.css.pct
import streetlight.web.model.ChatRoom
import koala.dom.component.*
import koala.external.*
import koala.dom.layout.*
import streetlight.web.layout.BodyStyle
import streetlight.web.utility.element

fun RouteScope.viewChatRoom() {
    val model = app.get<ChatRoom>()
    val element = column(modify(BodyStyle.MainColumn, JustifyContentEnd)) {
        itemsBlock(model.messagesState, modify(Magic, Blur, SlideLeft)) { message ->
            textBlock("${message.source}: ${message.text}")
        }
        textField(
            field = model.messageState,
            mod = Width(100.pct),
            onEnterSubmit = model::sendMessage
        )
    }

    element.onView(model::setIsActive)
}