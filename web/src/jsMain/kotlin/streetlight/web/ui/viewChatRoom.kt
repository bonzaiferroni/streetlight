package streetlight.web.ui

import koala.css.*
import koala.dom.*
import streetlight.web.model.Streetlight

fun RenderContext.viewChatRoom(app: Streetlight) {
    val model = app.chatRoom
    val element = column(modify(JustifyEnd)) {
        itemsBlock(model.messagesFlow, modify(Magic, Blur, SlideLeft), true) { message ->
            textBlock("${message.source}: ${message.text}")
        }
        textField(
            bindFlow = model.sendFlow,
            onChangeValue = model::setMessage,
            modifiers = modify(Width100),
            onEnter = model::sendMessage
        )
    }

    element.onView(model::setIsActive)
}