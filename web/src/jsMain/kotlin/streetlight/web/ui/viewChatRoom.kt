package streetlight.web.ui

import koala.modifier.*
import koala.dom.*
import kotlinx.css.pct
import streetlight.web.model.ChatRoom

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