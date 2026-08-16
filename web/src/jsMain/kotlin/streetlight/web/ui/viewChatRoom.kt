package streetlight.web.ui

import koala.css.*
import koala.dom.*
import streetlight.web.model.ChatRoom

fun RouteScope.viewChatRoom() {
    val model = app.get<ChatRoom>()
    val element = column(modify(BodyStyle.Column, JustifyContentEnd)) {
        itemsBlock(model.messagesFlow, modify(Magic, Blur, SlideLeft)) { message ->
            textBlock("${message.source}: ${message.text}")
        }
        textField(
            field = model.messageField,
            mod = modify(Width100P),
            onEnter = model::sendMessage
        )
    }

    element.onView(model::setIsActive)
}