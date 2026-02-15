package streetlight.web

import koala.css.*
import koala.dom.*

fun RenderContext.viewChatRoom(app: AppContext) {
    val model = app.chatRoom
    val element = column(modify(JustifyEnd)) {
        itemsBlock(model.messagesFlow, modify(Animate, MagicBlur, SlideX), true) { message ->
            textBlock("${message.source}: ${message.text}")
        }
        textField(
            binding = model.sendFlow,
            onChangeValue = model::setMessage,
            modifiers = modify(Width100),
            onEnter = model::sendMessage
        )
    }

    element.onView(model::setIsActive)
}