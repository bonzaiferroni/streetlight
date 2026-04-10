package streetlight.web.ui

import koala.css.*
import koala.dom.*
import streetlight.web.model.Streetlight

fun RenderContext.viewChatRoom(app: Streetlight) {
    val model = app.chatRoom
    val element = column(modify(JustifyContentEnd)) {
        itemsBlock(model.messagesFlow, modify(Magic, Blur, SlideLeft)) { message ->
            textBlock("${message.source}: ${message.text}")
        }
        textField(
            flow = model.sendFlow,
            onValue = model::setMessage,
            modifiers = modify(Width100P),
            onEnter = model::sendMessage
        )
    }

    element.onView(model::setIsActive)
}