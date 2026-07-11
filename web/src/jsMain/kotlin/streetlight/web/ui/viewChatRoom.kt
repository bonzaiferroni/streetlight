package streetlight.web.ui

import koala.css.*
import koala.dom.*
import streetlight.web.model.ChatRoom

fun AppScope.viewChatRoom() {
    val model = app.get<ChatRoom>()
    val element = column(modify(JustifyContentEnd)) {
        itemsBlock(model.messagesFlow, modify(Magic, Blur, SlideLeft)) { message ->
            textBlock("${message.source}: ${message.text}")
        }
        textField(
            flow = model.sendFlow,
            onValue = model::setMessage,
            mod = modify(Width100P),
            onEnter = model::sendMessage
        )
    }

    element.onView(model::setIsActive)
}